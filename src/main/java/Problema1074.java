import java.util.Scanner;

public class Problema1074 {
    public static void main(String[] args){
        Scanner leitor = new Scanner(System.in);
        int N = Integer.parseInt(leitor.nextLine());
        for (int K=0; K<N; K++){
            int Numero = Integer.parseInt(leitor.nextLine());
            if (Numero==0) {
                System.out.println("NULL");
            }else if(Numero>0){
                if(Numero %2 ==0){
                    System.out.println("EVEN POSITIVE");
                }else{
                    System.out.println("ODD POSITIVE");
                }
            }else{
                if(Numero%2 ==0){
                    System.out.println("EVEN NEGATIVE");
                } else{System.out.println("ODD NEGATIVE");
                }

            }
        }
        leitor.close();
    }
}