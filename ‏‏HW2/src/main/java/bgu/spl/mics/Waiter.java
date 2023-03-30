package bgu.spl.mics;

public class Waiter {

    public Waiter(){

    }

    public synchronized void justWait(){

        try {
            this.wait(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public void justWait(int v) {
        try {
            this.wait(v);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
