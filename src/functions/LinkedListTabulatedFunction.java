package functions;

import java.io.Serializable;

public class LinkedListTabulatedFunction implements TabulatedFunction, Serializable {
    // Внутренний класс для узлов списка
    static class FunctionNode implements Serializable {
        private transient FunctionPoint point;
        private transient FunctionNode prev;
        private transient FunctionNode next;

        public FunctionNode(FunctionPoint point) {
            this.point = point;
        }
        public FunctionNode(double x, double y) {
            this.point = new FunctionPoint(x, y);
        }
        public FunctionPoint getPoint() {
            return point;
        }
        public void setPoint(FunctionPoint point) {
            this.point = point;
        }
        public FunctionNode getPrev() {
            return prev;
        }
        public void setPrev(FunctionNode prev) {
            this.prev = prev;
        }
        public FunctionNode getNext() {
            return next;
        }
        public void setNext(FunctionNode next) {
            this.next = next;
        }
    }

    // Поля основного класса
    private transient FunctionNode head; // Голова списка (не содержит данных)
    private int size; // Количество значащих элементов
    private transient FunctionNode lastAccessedNode; // Для оптимизации доступа
    private transient int lastAccessedIndex; // Индекс последнего доступного узла

    // Инициализация пустого списка с головой
    private void initializeList() {
        head = new FunctionNode(new FunctionPoint(0, 0));
        head.setNext(head);
        head.setPrev(head);
        size = 0;
        lastAccessedNode = head;
        lastAccessedIndex = -1;
    }
    // Метод для получения узла по индексу с оптимизацией
    private FunctionNode getNodeByIndex(int index) {
        if (index < 0 || index >= size) {
            throw new FunctionPointIndexOutOfBoundsException("Индекс выходит за пределы: " + index);
        }
        // Оптимизация: начинаем поиск с последнего доступного узла
        FunctionNode current;
        int startIndex;
        if (lastAccessedIndex != -1 && Math.abs(index - lastAccessedIndex) < Math.min(index, size - index)) {
            // Ближе к последнему доступному узлу
            current = lastAccessedNode;
            startIndex = lastAccessedIndex;
        }
        else if (index < size / 2) {
            // Ближе к началу
            current = head.getNext();
            startIndex = 0;
        }
        else {
            // Ближе к концу
            current = head.getPrev();
            startIndex = size - 1;
        }
        // Поиск нужного узла
        if (index > startIndex) {
            for (int i = startIndex; i < index; i++) {
                current = current.getNext();
            }
        }
        else if (index < startIndex) {
            for (int i = startIndex; i > index; i--) {
                current = current.getPrev();
            }
        }
        // Сохраняем для будущей оптимизации
        lastAccessedNode = current;
        lastAccessedIndex = index;
        return current;
    }
    // Метод для добавления узла по индексу
    private FunctionNode addNodeByIndex(int index) {
        if (index < 0 || index > size) {
            throw new FunctionPointIndexOutOfBoundsException("Индекс выходит за пределы: " + index);
        }
        FunctionNode newNode = new FunctionNode(new FunctionPoint(0, 0));
        if (size == 0) {
            // Первый элемент
            newNode.setNext(head);
            newNode.setPrev(head);
            head.setNext(newNode);
            head.setPrev(newNode);
        }
        else if (index == size) {
            // Вставка в конец
            FunctionNode last = head.getPrev();
            last.setNext(newNode);
            newNode.setPrev(last);
            newNode.setNext(head);
            head.setPrev(newNode);
        }
        else {
            // Вставка в середину
            FunctionNode current = getNodeByIndex(index);
            FunctionNode prevNode = current.getPrev();
            prevNode.setNext(newNode);
            newNode.setPrev(prevNode);
            newNode.setNext(current);
            current.setPrev(newNode);
        }
        size++;
        lastAccessedIndex = -1; // Сбрасываем кэш
        return newNode;
    }
    // Метод для добавления узла в конец списка
    private FunctionNode addNodeToTail() {
        return addNodeByIndex(size);
    }
    // Метод для удаления узла по индексу
    private FunctionNode deleteNodeByIndex(int index) {
        if (index < 0 || index >= size) {
            throw new FunctionPointIndexOutOfBoundsException("Индекс выходит за пределы: " + index);
        }
        if (size < 3) {
            throw new IllegalStateException("Невозможно удалить точку — требуется минимум 2 точки");
        }
        FunctionNode nodeToDelete = getNodeByIndex(index);
        FunctionNode prevNode = nodeToDelete.getPrev();
        FunctionNode nextNode = nodeToDelete.getNext();
        prevNode.setNext(nextNode);
        nextNode.setPrev(prevNode);
        size--;
        lastAccessedIndex = -1; // Сбрасываем кэш
        return nodeToDelete;
    }

    // Конструкторы
    public LinkedListTabulatedFunction(double leftX, double rightX, int pointsCount) {
        if (leftX >= rightX) {
            throw new IllegalArgumentException("Левая граница должна быть меньше правой границы: " + leftX + " >= " + rightX);
        }
        if (pointsCount < 2) {
            throw new IllegalArgumentException("Количество баллов должно быть не менее 2: " + pointsCount);
        }

        initializeList();
        double step = (rightX - leftX) / (pointsCount - 1);
        for (int i = 0; i < pointsCount; i++) {
            double x = leftX + i * step;
            addNodeToTail().setPoint(new FunctionPoint(x, 0.0));
        }
    }
    public LinkedListTabulatedFunction(double leftX, double rightX, double[] values) {
        if (leftX >= rightX) {
            throw new IllegalArgumentException("Левая граница должна быть меньше правой границы: " + leftX + " >= " + rightX);
        }
        if (values.length < 2) {
            throw new IllegalArgumentException("Количество баллов должно быть не менее 2: " + values.length);
        }

        initializeList();
        double step = (rightX - leftX) / (values.length - 1);
        for (int i = 0; i < values.length; i++) {
            double x = leftX + i * step;
            addNodeToTail().setPoint(new FunctionPoint(x, values[i]));
        }
    }
    public LinkedListTabulatedFunction(FunctionPoint[] points) {
        if (points.length < 2) {
            throw new IllegalArgumentException("Требуется как минимум 2 точки");
        }
        for (int i = 1; i < points.length; i++) {
            if (points[i].getX() <= points[i - 1].getX()) {
                throw new IllegalArgumentException("Точки должны быть упорядочены по возрастанию X");
            }
        }

        initializeList();
        for (FunctionPoint point : points) {
            addNodeToTail().setPoint(new FunctionPoint(point));
        }
    }

    // Реализация методов
    @Override
    public double getLeftDomainBorder() {
        if (size == 0) {
            throw new IllegalStateException("Нет точек в функции");
        }
        return head.getNext().getPoint().getX();
    }
    @Override
    public double getRightDomainBorder() {
        if (size == 0) {
            throw new IllegalStateException("Нет точек в функции");
        }
        return head.getPrev().getPoint().getX();
    }
    @Override
    public double getFunctionValue(double x) {
        if (x < getLeftDomainBorder() || x > getRightDomainBorder()) {
            return Double.NaN;
        }

        // Поиск интервала для интерполяции
        for (int i = 0; i < size - 1; i++) {
            FunctionNode current = getNodeByIndex(i);
            FunctionNode next = current.getNext();

            double x1 = current.getPoint().getX();
            double x2 = next.getPoint().getX();
            double y1 = current.getPoint().getY();
            double y2 = next.getPoint().getY();

            if (x >= x1 && x <= x2) {
                if (x1 == x2) {
                    return y1; // Вертикальный отрезок
                }
                // Линейная интерполяция
                return y1 + (y2 - y1) * (x - x1) / (x2 - x1);
            }
        }

        return Double.NaN;
    }
    @Override
    public int getPointsCount() {
        return size;
    }
    @Override
    public FunctionPoint getPoint(int index) {
        FunctionNode node = getNodeByIndex(index);
        return new FunctionPoint(node.getPoint().getX(), node.getPoint().getY());
    }
    @Override
    public void setPoint(int index, FunctionPoint point) throws InappropriateFunctionPointException {
        if (index < 0 || index >= size) {
            throw new FunctionPointIndexOutOfBoundsException("Индекс выходит за пределы: " + index);
        }
        // Проверка порядка X
        if (index > 0 && point.getX() <= getPoint(index - 1).getX()) {
            throw new InappropriateFunctionPointException("X должен строго возрастать");
        }
        if (index < size - 1 && point.getX() >= getPoint(index + 1).getX()) {
            throw new InappropriateFunctionPointException("X должен строго возрастать");
        }

        FunctionNode node = getNodeByIndex(index);
        node.setPoint(new FunctionPoint(point.getX(), point.getY()));
    }
    @Override
    public double getPointX(int index) {
        return getPoint(index).getX();
    }
    @Override
    public void setPointX(int index, double x) throws InappropriateFunctionPointException {
        FunctionPoint point = getPoint(index);
        setPoint(index, new FunctionPoint(x, point.getY()));
    }
    @Override
    public double getPointY(int index) {
        return getPoint(index).getY();
    }
    @Override
    public void setPointY(int index, double y)  {
        FunctionNode node = getNodeByIndex(index);
        FunctionPoint newPoint = new FunctionPoint(node.getPoint().getX(), y);
        node.setPoint(newPoint);
    }
    @Override
    public void deletePoint(int index) {
        deleteNodeByIndex(index);
    }
    @Override
    public void addPoint(FunctionPoint point) throws InappropriateFunctionPointException {
        // Находим позицию для вставки
        int insertIndex = size;
        for (int i = 0; i < size; i++) {
            double currentX = getPointX(i);
            if (currentX == point.getX()) {
                throw new InappropriateFunctionPointException("Точка с X=" + point.getX() + " уже существует");
            }
            if (currentX > point.getX()) {
                insertIndex = i;
                break;
            }
        }
        FunctionNode newNode = addNodeByIndex(insertIndex);
        newNode.setPoint(point);
    }

    // Специальные методы для сериализации
    private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        // Сериализуем обычные поля
        out.defaultWriteObject();
        out.writeInt(size);

        // Сериализуем точки
        FunctionNode current = head.getNext();
        for (int i = 0; i < size; i++) {
            out.writeDouble(current.getPoint().getX());
            out.writeDouble(current.getPoint().getY());
            current = current.getNext();
        }
    }
    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
        // Десериализуем обычные поля
        in.defaultReadObject();
        int savedSize = in.readInt();
        initializeList();

        // Восстанавливаем точки
        for (int i = 0; i < savedSize; i++) {
            double x = in.readDouble();
            double y = in.readDouble();
            addNodeToTail().setPoint(new FunctionPoint(x, y));
        }
    }
}