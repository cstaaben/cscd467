import java.awt.*;
import java.util.concurrent.Semaphore;

class Fork {

  private PhilCanvas display;
  private int identity;
  private Semaphore semaphore = new Semaphore(1);
  
  Fork(PhilCanvas disp, int id)
    { display = disp; identity = id;}

  public void put() {
    semaphore.release();
    display.setFork(identity,false);
  }

  public void get() throws java.lang.InterruptedException {
	semaphore.acquire();
	display.setFork(identity,true);
  }
}
