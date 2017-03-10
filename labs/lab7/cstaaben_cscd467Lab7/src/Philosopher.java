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
        view.setPhil(identity,PhilCanvas.THINKING);
        sleep(controller.sleepTime());
        //hungry
        view.setPhil(identity,PhilCanvas.HUNGRY);
        if(identity != 0)
        {
          try {
            
          }
          right.get();
          view.setPhil(identity,PhilCanvas.GOTRIGHT);
        }
        else
        {
          left.get();
          view.setPhil(identity,PhilCanvas.GOTLEFT);
        }
        //gotright chopstick
        sleep(500);
        if((identity % 2) == 0)
          left.get();
        else
          right.get();
        //eating
        view.setPhil(identity,PhilCanvas.EATING);
        sleep(controller.eatTime());
        right.put();
        left.put();
        view.setPhil(identity, PhilCanvas.THINKING);
      }
    } catch (java.lang.InterruptedException e) {}
  }
}
