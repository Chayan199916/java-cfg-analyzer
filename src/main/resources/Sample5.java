public class Sample5 {
    public static void main(String[] args) {
        int i = 1;
        while (i < 5) {
            i = i + 2;
            i = i + 1;
        }
        if (i < 5) {
            i++;
            i += 2;
        } else {
            i--;
            i -= 2;
        }
    }
}
