import java.util.Scanner;

public class Problema1009 {
    public static void main(String[] args){
        Scanner leitor = new Scanner(System.in);
        String Vendedor = leitor.nextLine();
        double Salario = Double.parseDouble(leitor.nextLine());
        double Vendas = Double.parseDouble(leitor.nextLine());
        double Total = Salario + (Vendas*0.15);
        System.out.printf("TOTAL = R$ %.2f\n", Total);
        leitor.close();
    }
}