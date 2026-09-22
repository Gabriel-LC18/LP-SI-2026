import java.util.Scanner;

public class Problema1006 {
    public static void main(String[] args) {


        Scanner leitor = new Scanner(System.in);
        double A = Double.parseDouble(leitor.nextLine());
        double B = Double.parseDouble(leitor.nextLine());
        double C = Double.parseDouble(leitor.nextLine());
        double MEDIA = ((A * 2) + (B * 3) + (C * 5)) / 10;
        System.out.printf("MEDIA = %.1f\n", MEDIA);
        leitor.close();
    }
}
