public class test {
    public static void main(String[] args) {
        int x0 = 1;
        int i = 1;
        int x1 = 262144;
        int t = x1 - 1;
        while(t != 0){
            i = i + 2;
            t = i;
            while(t != 0){
                x0 = x0 + 1;
                t = t-1;
            }
            t = x1;
            int k = x0;
            while(k != 0){
                k = k - 1;
                t = t-1;
            }
        }
        System.out.println(x0);
    }

}
