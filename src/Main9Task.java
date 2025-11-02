import functions.*;
import functions.basic.*;
import java.io.*;

public class Main9Task {
    public static void main(String[] args) {
        try {
            // Создаем табулированную функцию: ln(exp(x)) = x на отрезке [0, 10] с 11 точками
            System.out.println("=== ТЕСТИРОВАНИЕ СЕРИАЛИЗАЦИИ ===");

            // Создаем композицию: ln(exp(x))
            Function exp = new Exp();
            Function ln = new Log(Math.E);
            Function composition = Functions.composition(ln, exp);

            // Табулируем функцию
            TabulatedFunction tabulatedFunc = TabulatedFunctions.tabulate(composition, 0, 10, 11);

            System.out.println("Исходная функция (ln(exp(x)) = x):");
            printFunctionValues(tabulatedFunc, 0, 10, 1);

            // Тестируем Serializable
            testSerializable(tabulatedFunc, "serializable_function.ser");

            // Тестируем Externalizable
            testExternalizable(tabulatedFunc, "externalizable_function.ser");

            // Сравниваем размеры файлов
            compareFileSizes();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void testSerializable(TabulatedFunction func, String filename) {
        System.out.println("\n=== ТЕСТИРОВАНИЕ Serializable ===");

        try {
            // Сериализация
            FileOutputStream fileOut = new FileOutputStream(filename);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(func);
            out.close();
            fileOut.close();
            System.out.println("Сериализованные данные сохранены в " + filename);

            // Десериализация
            FileInputStream fileIn = new FileInputStream(filename);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            TabulatedFunction deserializedFunc = (TabulatedFunction) in.readObject();
            in.close();
            fileIn.close();

            System.out.println("Восстановленная функция:");
            printFunctionValues(deserializedFunc, 0, 10, 1);

            // Проверяем эквивалентность
            System.out.println("Функции эквивалентны: " + functionsEqual(func, deserializedFunc));

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    private static void testExternalizable(TabulatedFunction func, String filename) {
        System.out.println("\n=== ТЕСТИРОВАНИЕ Externalizable ===");

        try {
            // Создаем Externalizable версию
            ArrayTabulatedFunctionExternalizable externalizableFunc =
                    new ArrayTabulatedFunctionExternalizable(0, 10, 11);

            // Копируем точки из исходной функции
            for (int i = 0; i < func.getPointsCount(); i++) {
                externalizableFunc.setPoint(i, func.getPoint(i));
            }

            // Сериализация
            FileOutputStream fileOut = new FileOutputStream(filename);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(externalizableFunc);
            out.close();
            fileOut.close();
            System.out.println("Сериализованные данные сохранены в " + filename);

            // Десериализация
            FileInputStream fileIn = new FileInputStream(filename);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            ArrayTabulatedFunctionExternalizable deserializedFunc =
                    (ArrayTabulatedFunctionExternalizable) in.readObject();
            in.close();
            fileIn.close();

            System.out.println("Восстановленная функция:");
            printFunctionValues(deserializedFunc, 0, 10, 1);

            // Проверяем эквивалентность
            System.out.println("Функции эквивалентны: " + functionsEqual(func, deserializedFunc));

        } catch (IOException | ClassNotFoundException | InappropriateFunctionPointException e) {
            e.printStackTrace();
        }
    }
    private static void printFunctionValues(TabulatedFunction func, double from, double to, double step) {
        System.out.println("x\t\tf(x)");
        System.out.println("-------------------");
        for (double x = from; x <= to + 1e-10; x += step) {
            System.out.printf("%-8.1f\t%-12.6f%n", x, func.getFunctionValue(x));
        }
    }
    private static boolean functionsEqual(TabulatedFunction func1, TabulatedFunction func2) {
        if (func1.getPointsCount() != func2.getPointsCount()) {
            return false;
        }

        for (int i = 0; i < func1.getPointsCount(); i++) {
            if (Math.abs(func1.getPointX(i) - func2.getPointX(i)) > 1e-10 ||
                    Math.abs(func1.getPointY(i) - func2.getPointY(i)) > 1e-10) {
                return false;
            }
        }

        return true;
    }
    private static void compareFileSizes() {
        System.out.println("\n=== СРАВНЕНИЕ РАЗМЕРОВ ФАЙЛОВ ===");
        File serializableFile = new File("serializable_function.ser");
        File externalizableFile = new File("externalizable_function.ser");

        System.out.println("Размер файла Serializable: " + serializableFile.length() + " байт");
        System.out.println("Размер файла Externalizable: " + externalizableFile.length() + " байт");

        long diff = serializableFile.length() - externalizableFile.length();
        System.out.println("Разница: " + Math.abs(diff) + " байт");
        if (diff > 0) {
            System.out.println("Externalizable файл меньше на " + diff + " байт");
        } else if (diff < 0) {
            System.out.println("Serializable файл меньше на " + Math.abs(diff) + " байт");
        } else {
            System.out.println("Файлы одинакового размера");
        }
    }
}