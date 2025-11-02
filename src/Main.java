import functions.*;
import functions.meta.*;
import functions.basic.*;
import java.io.*;

public class Main {
    public static void main(String[] args) {
        try {
            // 1. Тестирование классов Sin и Cos
            System.out.println("=== ТЕСТИРОВАНИЕ SIN И COS ===");
            testSinCos();

            // 2. Тестирование табулирования и суммы квадратов
            System.out.println("\n=== ТЕСТИРОВАНИЕ ТАБУЛИРОВАНИЯ И СУММЫ КВАДРАТОВ ===");
            testTabulationAndSum();

            // 3. Тестирование текстовой сериализации экспоненты
            System.out.println("\n=== ТЕСТИРОВАНИЕ ТЕКСТОВОЙ СЕРИАЛИЗАЦИИ (ЭКСПОНЕНТА) ===");
            testTextSerialization();

            // 4. Тестирование бинарной сериализации логарифма
            System.out.println("\n=== ТЕСТИРОВАНИЕ БИНАРНОЙ СЕРИАЛИЗАЦИИ (ЛОГАРИФМ) ===");
            testBinarySerialization();

        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void testSinCos() {
        // Используем готовые классы из functions.basic
        Function sin = new Sin();
        Function cos = new Cos();

        double from = 0;
        double to = Math.PI;
        double step = 0.1;

        System.out.println("Сравнение Sin и Cos на отрезке [0, π]:");
        System.out.println("x\t\tSin(x)\t\tCos(x)");
        System.out.println("----------------------------------------");

        for (double x = from; x <= to + 1e-10; x += step) {
            System.out.printf("%.3f\t\t%.6f\t%.6f%n",
                    x, sin.getFunctionValue(x), cos.getFunctionValue(x));
        }
    }
    private static void testTabulationAndSum() {
        double from = 0;
        double to = Math.PI;
        double step = 0.1;
        int pointsCount = 10;

        // Создаем табулированные аналоги используя готовые классы
        TabulatedFunction tabulatedSin = TabulatedFunctions.tabulate(new Sin(), from, to, pointsCount);
        TabulatedFunction tabulatedCos = TabulatedFunctions.tabulate(new Cos(), from, to, pointsCount);

        System.out.println("Сравнение оригинального Sin и табулированного:");
        System.out.println("x\t\tSin(x)\t\tTabSin(x)\tРазница");
        System.out.println("------------------------------------------------");

        for (double x = from; x <= to + 1e-10; x += step) {
            double original = new Sin().getFunctionValue(x);
            double tabulated = tabulatedSin.getFunctionValue(x);
            double diff = Math.abs(original - tabulated);
            System.out.printf("%.3f\t\t%.6f\t%.6f\t%.6f%n", x, original, tabulated, diff);
        }

        // Создаем сумму квадратов
        Function sinSquared = Functions.power(tabulatedSin, 2);
        Function cosSquared = Functions.power(tabulatedCos, 2);
        Function sumOfSquares = Functions.sum(sinSquared, cosSquared);

        System.out.println("\nСумма квадратов табулированных Sin и Cos:");
        System.out.println("x\t\tSin²+Cos²");
        System.out.println("-----------------------");

        for (double x = from; x <= to + 1e-10; x += step) {
            double value = sumOfSquares.getFunctionValue(x);
            System.out.printf("%.3f\t\t%.6f%n", x, value);
        }

        // Исследуем влияние количества точек
        System.out.println("\nИсследование влияния количества точек на точность:");
        int[] pointCounts = {5, 10, 20, 50};

        for (int count : pointCounts) {
            TabulatedFunction sinTab = TabulatedFunctions.tabulate(new Sin(), from, to, count);
            TabulatedFunction cosTab = TabulatedFunctions.tabulate(new Cos(), from, to, count);
            Function sum = Functions.sum(Functions.power(sinTab, 2), Functions.power(cosTab, 2));

            double maxError = 0;
            for (double x = from; x <= to + 1e-10; x += step) {
                double error = Math.abs(sum.getFunctionValue(x) - 1.0); // Теоретически должно быть 1
                maxError = Math.max(maxError, error);
            }
            System.out.printf("Точек: %d, Максимальная ошибка: %.8f%n", count, maxError);
        }
    }
    private static void testTextSerialization() {
        try {
            // Создаем табулированную экспоненту используя готовый класс
            Function exp = new Exp();
            TabulatedFunction tabulatedExp = TabulatedFunctions.tabulate(exp, 0, 10, 11);

            // Записываем в текстовый файл
            try (FileWriter writer = new FileWriter("exponential.txt")) {
                TabulatedFunctions.writeTabulatedFunction(tabulatedExp, writer);
            }

            // Читаем из текстового файла
            TabulatedFunction readExp;
            try (FileReader reader = new FileReader("exponential.txt")) {
                readExp = TabulatedFunctions.readTabulatedFunction(reader);
            }

            System.out.println("Сравнение оригинальной и восстановленной экспоненты:");
            System.out.println("x\t\t\tOriginal\t\t\tRead\t\t\t\tРазница");
            System.out.println("---------------------------------------------------------------");

            for (double x = 0; x <= 10; x += 1.0) {
                double original = tabulatedExp.getFunctionValue(x);
                double read = readExp.getFunctionValue(x);
                double diff = Math.abs(original - read);
                System.out.printf("%-8.1f\t%-16.6f\t%-16.6f\t%-12.6f%n", x, original, read, diff);
            }

            // Показываем содержимое файла
            System.out.println("\nСодержимое текстового файла exponential.txt:");
            try (BufferedReader br = new BufferedReader(new FileReader("exponential.txt"))) {
                String line;
                while ((line = br.readLine()) != null) {
                    System.out.println(line);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static void testBinarySerialization() {
        try {
            // Создаем табулированный логарифм используя готовый класс
            // (предполагая, что ваш класс Log уже корректно обрабатывает область определения)
            Function log = new Log(Math.E);
            TabulatedFunction tabulatedLog = TabulatedFunctions.tabulate(log, 0.1, 10, 11);

            // Записываем в бинарный файл
            try (FileOutputStream fos = new FileOutputStream("logarithm.bin")) {
                TabulatedFunctions.outputTabulatedFunction(tabulatedLog, fos);
            }

            // Читаем из бинарного файла
            TabulatedFunction readLog;
            try (FileInputStream fis = new FileInputStream("logarithm.bin")) {
                readLog = TabulatedFunctions.inputTabulatedFunction(fis);
            }

            System.out.println("Сравнение оригинального и восстановленного логарифма:");
            System.out.println("x\t\tOriginal\tRead\t\tРазница");
            System.out.println("-----------------------------------------------");

            for (double x = 0.1; x <= 10; x += 1.0) {
                double original = tabulatedLog.getFunctionValue(x);
                double read = readLog.getFunctionValue(x);
                double diff = Math.abs(original - read);
                System.out.printf("%.1f\t\t%.6f\t%.6f\t%.6f%n", x, original, read, diff);
            }

            // Показываем размер файла
            File file = new File("logarithm.bin");
            System.out.printf("\nРазмер бинарного файла: %d байт%n", file.length());

        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}