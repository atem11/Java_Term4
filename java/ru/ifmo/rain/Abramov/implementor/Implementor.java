package ru.ifmo.rain.Abramov.implementor;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.*;
import java.lang.reflect.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

//java info.kgeorgiy.java.advanced.implementor.Tester class ru.ifmo.rain.Abramov.implementor.Implementor
//java info.kgeorgiy.java.advanced.implementor.Tester jar-class ru.ifmo.rain.Abramov.implementor.Implementor


/**This class realized interfaces {@link Impler} and {@link JarImpler}.
 *
 * @author Artem Abramov.
 */
public class Implementor implements Impler, JarImpler {

    /**
     * Default constructor of {@link Implementor}.
     */
    public Implementor() {

    }

    /**
     * Use implement or implementJar.
     * If 2 argument, create implementation of class.
     * If 3 arguments, first must be '-jar', and create .jar with implementation of class.
     *
     * @param args 3 or 2 arguments. Last two must be name of class and root directory.
     */
    public static void main(String[] args) {
        if (args == null || args.length < 2 || args.length > 3) {
            System.err.println("Main need 3 or 2 argument");
            return;
        }
        for (String arg : args) {
            if (arg == null) {
                System.err.println("Arg must be not 'null'");
            }
        }
        Implementor impl = new Implementor();
        try {
            if (args.length == 2) {
                impl.implement(Class.forName(args[0]), Paths.get(args[1]));
            } else if (args[0].equals("-jar")) {
                impl.implementJar(Class.forName(args[1]), Paths.get(args[2]));
            } else {
                System.err.println("first arg must be '-jar'");
            }
        } catch (InvalidPathException e) {
            System.err.println("Incorrect path to root: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.err.println("Incorrect class name: " + e.getMessage());
        } catch (ImplerException e) {
            System.err.println(e.getMessage());
        }
    }

    /**
     * local output file
     */
    private BufferedWriter myOutput;
    /**
     * local copy of {@link Class}
     */
    private Class<?> myToken;
    /**
     * local name of class
     */
    private String className;

    /**
     * {@link String} which separate line in files
     */
    private static final String SEP = System.lineSeparator();
    /**
     * {@link String} with intend.
     */
    private static final String TAB = "    ";
    /**
     * {@link Map} of default return values.
     */
    private static final Map<Class<?>, String> returns = new HashMap<>();

    static {
        returns.put(byte.class, "0");
        returns.put(short.class, "0");
        returns.put(int.class, "0");
        returns.put(long.class, "0L");
        returns.put(float.class, "0.0f");
        returns.put(double.class, "0.0d");
        returns.put(char.class, "'\\u0000'");
        returns.put(boolean.class, "false");
    }

    /**
     * New {@link Method} for {@link HashSet}
     */
    private class MethodSet {

        Method method;

        /**
         * Basic constructor
         *
         * @param method that method save in new MethodSet
         */
        MethodSet(Method method) {
            this.method = method;
        }

        /**
         * Test for equality
         *
         * @param m other value
         * @return {@code true} if {@link MethodSet} equals, {@code false} otherwise
         */
        @Override
        public boolean equals(Object m) {
            if (!(m instanceof MethodSet)) {
                return false;
            }
            MethodSet newMethod = (MethodSet) m;
            return method.getName().equals(newMethod.method.getName())
                    && Arrays.equals(method.getParameterTypes(), newMethod.method.getParameterTypes());
        }

        /**
         * Generate hash for {@link Method}
         *
         * @return hashcode
         */
        @Override
        public int hashCode() {
            return (method.getName() + Arrays.toString(method.getParameterTypes())).hashCode();
        }
    }

    /**
     * Made new {@link String} with UTF-8 charset.
     *
     * @param in String in UTF-8.
     * @return String in unicode.
     */
    private String toUnicode(String in) {
        StringBuilder b = new StringBuilder();

        for (char c : in.toCharArray()) {
            if (c >= 128)
                b.append("\\u" + String.format("%04X", (int) c));
            else
                b.append(c);
        }

        return b.toString();
    }

    /**
     * generate {@link String} {@link Package} of {@link Class} or Interface.
     *
     * @return path converted to String
     */
    private String getPack(String c) {
        return myToken.getPackage().getName().replace(".", c);
    }

    /**
     * Generate ".jar" file with compiled {@link Class}.
     *  ".jar" located to {@link Path} path.
     *
     *
     * @param aClass create implementation and .jar file for.
     * @param path   where create ".jar".
     * @throws ImplerException if .jar can't be create.
     */
    @Override
    public void implementJar(Class<?> aClass, Path path) throws ImplerException {
        Path tmp = Paths.get(".").resolve(".Impl");
        try {
            Files.createDirectories(tmp);
        } catch (IOException e) {
            throw new ImplerException("can't create temporary class");
        }
        implement(aClass, tmp);
        Path fold = tmp.resolve(getPack(File.separator));
        Path javaFile = fold.resolve(myToken.getSimpleName() + "Impl.java");

        try {
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            if (compiler.run(null, null, null, javaFile.toString(), "-cp", System.getProperty("java.class.path")) != 0) {
                throw new ImplerException("can't compile temporary class");
            }
        } catch (NullPointerException e) {
            throw new ImplerException("can't found java compiler");
        }
        Path classFile = fold.resolve(myToken.getSimpleName() + "Impl.class");

        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        try {
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
        } catch (IOException e) {
            throw new ImplerException("kek");
        }
        try (JarOutputStream out = new JarOutputStream(Files.newOutputStream(path), manifest)) {
            BufferedInputStream in = new BufferedInputStream(new FileInputStream(classFile.toString()));
            out.putNextEntry(new JarEntry(getPack("/") + "/" + myToken.getSimpleName() + "Impl.class"));
            int read;
            while ((read = in.read()) != -1) {
                out.write(read);
            }
            out.closeEntry();
            in.close();
        } catch (IOException e) {
            throw new ImplerException("can't write into Jar file");
        } finally {
            try {
                Files.walkFileTree(tmp, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Files.delete(file);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        Files.delete(dir);
                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (IOException e) {
                throw new ImplerException("can't clean temporary directory");
            }
        }
    }

    /**
     * @param token create implementation for.
     * @param root  root directory.
     * @throws ImplerException if implementation can't be create.
     */
    @Override
    public void implement(Class<?> token, Path root) throws ImplerException {
        myToken = token;
        if (myToken.isPrimitive() || myToken.isArray() || myToken == java.lang.Enum.class) {
            throw new ImplerException(token.getName() + " - is not a class or interface");
        }
        if (Modifier.isFinal(myToken.getModifiers())) {
            throw new ImplerException("It is a final class" + myToken.getName());
        }
        createFile(root);
    }

    /**
     * Create file {@link Class}.name + "Impl.java" suffix.
     *
     * @param out root directory.
     * @throws ImplerException if implementation can't be create.
     */
    private void createFile(Path out) throws ImplerException {
        try {
            String packag = "";
            if (myToken.getPackage() != null) {
                packag = getPack(File.separator);
            }
            out = out.resolve(packag);
            try {
                Files.createDirectories(out);
            } catch (IOException e) {
                throw new ImplerException(e);
            }
            className = myToken.getSimpleName() + "Impl";
            out = out.resolve(className + ".java");
            try (BufferedWriter myOutput1 = Files.newBufferedWriter(out)) {
                myOutput = myOutput1;
                createCode();
            }
        } catch (IOException e) {
            throw new ImplerException(e);
        }
    }

    /**
     * Print body of implementation {@link Class} or Interface.
     *
     * @throws IOException     if can't write in file.
     * @throws ImplerException if implementation can't be create.
     */
    private void createCode() throws IOException, ImplerException {
        if (myToken.getPackage() != null) {
            myOutput.write(toUnicode("package " + myToken.getPackage().getName() + ';' + SEP + SEP));
        }


        myOutput.write(toUnicode(
                Modifier.toString(myToken.getModifiers() & ~Modifier.ABSTRACT & ~Modifier.INTERFACE)
                        + " class " + className +
                        (myToken.isInterface() || myToken.isAnnotation() ? " implements " : " extends ") +
                        myToken.getSimpleName() + " {" + SEP + SEP)
        );


        boolean construct = false;
        for (Constructor<?> constructor : myToken.getDeclaredConstructors()) {
            if (!Modifier.isPrivate(constructor.getModifiers())) {
                construct = true;
                printHead(className, constructor);

                myOutput.write(toUnicode(TAB + TAB + "super("));
                myOutput.write(toUnicode(Arrays.stream(constructor.getParameters())
                        .map(Parameter::getName).collect(Collectors.joining(", "))));
                myOutput.write(toUnicode(");" + SEP));

                myOutput.write(toUnicode(TAB + "}" + SEP + SEP));
            }
        }
        if (!construct && !myToken.isInterface()) {
            throw new ImplerException("No public constructors in class: " + myToken.getSimpleName());
        }


        Class<?> superClass = myToken;
        HashSet<MethodSet> methods = new HashSet<>();

        for (Method m : superClass.getMethods()) {
            methods.add(new MethodSet(m));
        }

        while (superClass != null && !superClass.equals(Object.class)) {
            for (Method m : superClass.getDeclaredMethods()) {
                methods.add(new MethodSet(m));
            }
            superClass = superClass.getSuperclass();
        }

        for (MethodSet myMethod : methods) {
            Method method = myMethod.method;
            if (Modifier.isAbstract(method.getModifiers())) {
                myOutput.write(toUnicode(TAB + "@Override" + " "));
                if (method.getDeclaredAnnotations().length != 0) {
                    myOutput.write(toUnicode(Arrays.stream(method.getDeclaredAnnotations()).map(a -> "@" + a.annotationType().getSimpleName()).collect(Collectors.joining(" "))));
                }
                myOutput.write(toUnicode(SEP));
                printHead(method.getName(), method);

                if (!method.getReturnType().equals(void.class)) {
                    myOutput.write(toUnicode(TAB + TAB + "return "));
                    myOutput.write(toUnicode(returns.getOrDefault(method.getReturnType(), "null")));
                    myOutput.write(toUnicode(";" + SEP));
                }

                myOutput.write(toUnicode(TAB + "}" + SEP + SEP));
            }
        }

        myOutput.write(toUnicode("}"));
    }

    /**
     * Print signature of {@link Executable} to file.
     *
     * @param name name of {@link Method} or {@link Constructor}
     * @param ex   {@link Method} or {@link Constructor}
     * @throws IOException if can't write in file.
     */
    private void printHead(String name, Executable ex) throws IOException {

        myOutput.write(toUnicode(TAB +
                Modifier.toString(ex.getModifiers()
                        & ~Modifier.ABSTRACT & ~Modifier.INTERFACE & ~Modifier.TRANSIENT)
                + " ")
        );

        if (ex instanceof Method) {
            myOutput.write(toUnicode(((Method) ex).getReturnType().getCanonicalName() + " "));
        }
        myOutput.write(toUnicode(name + "("));

        myOutput.write(toUnicode(
                Arrays.stream(ex.getParameters()).map(p -> p.getType().getCanonicalName() + " " + p.getName())
                        .collect(Collectors.joining(", ")))
        );

        myOutput.write(toUnicode(")"));

        Class<?> except[] = ex.getExceptionTypes();
        if (except.length > 0) {
            myOutput.write(toUnicode(" throws"));
            myOutput.write(toUnicode(" " + except[0].getCanonicalName()));
            for (int i = 1; i < except.length; ++i) {
                myOutput.write(toUnicode(", " + except[i].getCanonicalName()));
            }
        }

        myOutput.write(toUnicode(" {" + SEP));
    }
}
