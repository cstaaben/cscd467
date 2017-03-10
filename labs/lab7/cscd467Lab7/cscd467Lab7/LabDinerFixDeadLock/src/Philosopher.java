import java.awt.*;

class Philosopher extends Thread {
  private int identity;
  private PhilCanvas view;
  private Diners controller;
  private Fork left;
  private Fork right;

  Philosopher(Diners ctr, int id, Fork l, Fork r) {
    controller = ctr; view = ctr.display;
    identity = id; left = l; right = r;
  }

  public void run() {
    try {
      while (true) {
        //thinking
        view.setPhil(identity,view.THINKING);
        sleep(controller.sleepTime());
        //hungry
        view.setPhil(identity,view.HUNGRY);
        if((identity % 2) == 0)
        {
        	right.get();
        	view.setPhil(identity,view.GOTRIGHT);
        }
        else
        {
        	left.get();
        	view.setPhil(identity,view.GOTLEFT);
        }
        //gotright chopstick
        sleep(500);
        if((identity % 2) == 0)
        	left.get();
        else
        	right.get();
        //eating
        view.setPhil(identity,view.EATING);
        sleep(controller.eatTime());
        right.put();
        left.put();
        view.setPhil(identity, view.THINKING);
      }
    } catch (java.lang.InterruptedException e) {}
  }
}
