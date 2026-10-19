import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.jar.JarFile;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.pool.TypePool;

import necesse.engine.modLoader.annotations.ModConstructorPatch;
import necesse.engine.modLoader.annotations.ModMethodPatch;

import static net.bytebuddy.matcher.ElementMatchers.isConstructor;
import static net.bytebuddy.matcher.ElementMatchers.isMethod;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.takesArguments;

/**
 * Offline check that every patch class applies to the real game jar, the same way the game's mod loader builds it.
 * Usage: java -cp "Necesse.jar;lib/*;mod classes;tools out" WeaveCheck Necesse.jar PatchClass...
 * For each patch it weaves the advice into the game class, checks the advice was actually inlined, then defines the
 * woven class with the bytecode verifier on, so a bad weave fails here and not in game.
 */
public class WeaveCheck {

    public static void main(String[] args) throws Exception {
        File gameJar = new File(args[0]);
        ClassFileLocator locator = new ClassFileLocator.Compound(
                new ClassFileLocator.ForJarFile(new JarFile(gameJar)), ClassFileLocator.ForClassLoader.ofSystemLoader());
        TypePool pool = TypePool.Default.of(locator);
        int failures = 0;

        for (int i = 1; i < args.length; i++) {
            Class<?> patch = Class.forName(args[i]);
            Class<?> target;
            ElementMatcher.Junction<net.bytebuddy.description.method.MethodDescription> matcher;
            ModMethodPatch mp = patch.getAnnotation(ModMethodPatch.class);
            ModConstructorPatch cp = patch.getAnnotation(ModConstructorPatch.class);
            if (mp != null) {
                target = mp.target();
                matcher = isMethod().and(named(mp.name())).and(takesArguments(mp.arguments()));
            } else if (cp != null) {
                target = cp.target();
                matcher = isConstructor().and(takesArguments(cp.arguments()));
            } else {
                System.out.println("FAIL " + patch.getName() + ": no patch annotation");
                failures++;
                continue;
            }

            try {
                DynamicType.Unloaded<?> out = new ByteBuddy()
                        .redefine(pool.describe(target.getName()).resolve(), locator)
                        .visit(Advice.to(patch).on(matcher))
                        .make();
                byte[] woven = out.getBytes();
                byte[] original = locator.locate(target.getName()).resolve();
                String hookClass = hookClassOf(patch);
                boolean changed = woven.length > original.length;
                boolean inlined = hookClass == null || contains(woven, hookClass) && !contains(original, hookClass);
                if (!changed || !inlined) {
                    System.out.println("FAIL " + patch.getSimpleName() + ": advice did not apply to " + target.getName());
                    failures++;
                    continue;
                }
                verify(target.getName(), woven, gameJar);
                System.out.println("OK   " + patch.getSimpleName() + " -> " + target.getSimpleName()
                        + " (" + original.length + " -> " + woven.length + " bytes, verified)");
            } catch (Throwable t) {
                System.out.println("FAIL " + patch.getSimpleName() + ": " + t);
                failures++;
            }
        }
        System.exit(failures == 0 ? 0 : 1);
    }

    /** The internal name of the first non-game class the advice calls, so we can look for it in the woven bytes. */
    private static String hookClassOf(Class<?> patch) throws IOException {
        byte[] bytes = ClassFileLocator.ForClassLoader.of(patch.getClassLoader()).locate(patch.getName()).resolve();
        String text = new String(bytes, StandardCharsets.ISO_8859_1);
        int from = 0;
        while (true) {
            int at = text.indexOf("settingsforge/", from);
            if (at < 0) {
                at = text.indexOf("statusforge/", from);
            }
            if (at < 0) {
                return null;
            }
            int end = at;
            while (end < text.length() && (Character.isLetterOrDigit(text.charAt(end)) || "/_$".indexOf(text.charAt(end)) >= 0)) {
                end++;
            }
            String name = text.substring(at, end);
            if (!name.equals(patch.getName().replace('.', '/'))) {
                return name;
            }
            from = end;
        }
    }

    private static boolean contains(byte[] haystack, String needle) {
        return new String(haystack, StandardCharsets.ISO_8859_1).contains(needle);
    }

    /** Defines the woven class in a throwaway loader and initializes it, which runs the bytecode verifier. */
    private static void verify(String className, final byte[] woven, File gameJar) throws Exception {
        ClassLoader parent = WeaveCheck.class.getClassLoader();
        ClassLoader loader = new ClassLoader(parent) {
            @Override
            protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                if (name.equals(className)) {
                    synchronized (getClassLoadingLock(name)) {
                        Class<?> c = findLoadedClass(name);
                        if (c == null) {
                            c = defineClass(name, woven, 0, woven.length);
                        }
                        if (resolve) {
                            resolveClass(c);
                        }
                        return c;
                    }
                }
                return super.loadClass(name, resolve);
            }
        };
        Class<?> c = Class.forName(className, true, loader);
        c.getDeclaredMethods();
    }
}
