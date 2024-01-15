public class Sample1 {
    private int value;

    public Sample1() {
        this.value = 0;
    }

    public void setValue(int newValue) {
        this.value = newValue;
    }

    public int getValue() {
        return value;
    }

    public static void main(String[] args) {
        Sample1 sample = new Sample1();
        sample.setValue(42);
        System.out.println("Current value: " + sample.getValue());
    }
}
