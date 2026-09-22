import java.util.Scanner;

public class Problema1165 {
    public static void main(String[] args) {
        Scanner leitor = new Scanner(System.in);
        int n = Integer.parseInt(leitor.nextLine());
        for (int i = 0; i < n; i++) {
            int num = Integer.parseInt(leitor.nextLine());
            boolean ehPrimo = true;
            for (int j = 2; j < num; j++) {
                if (num % j == 0) {
                    ehPrimo = false;
                    break;
                }
            }
            if (ehPrimo) {
                System.out.printf("%d eh primo\n", num);
            } else {
                System.out.printf("%d nao eh primo\n", num);
            }
        }
        leitor.close();
    }
}
