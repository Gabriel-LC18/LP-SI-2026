import java.util.Scanner;

public class Problema1134 {
    public static void main(String[] args) {
        Scanner leitor = new Scanner(System.in);
        System.out.println("MUITO OBRIGADO");
        int Alcool = 0;
        int Gasolina = 0;
        int Diesel = 0;
        int codigo = 0;
        while (codigo!= 4) {
            codigo = Integer.parseInt(leitor.nextLine());
            if (codigo == 1) {
                Alcool++;
            } else if (codigo == 2) {
                Gasolina++;
            } else if (codigo == 3) {
                Diesel++;
            }
        }
        System.out.printf("Alcool: %d\n", Alcool);
        System.out.printf("Gasolina: %d\n", Gasolina);
        System.out.printf("Diesel: %d\n", Diesel);
        leitor.close();

    }
}

