package CPSC559;

public class LoadBalancerMain {
    public static void main(String[] args) {
        new Thread(new LoadBalancer()).start();
    }
}