package io.github.openhelios.brick.plus;

public class MainWithShutdown {

  static {
    Runtime.getRuntime() //
        .addShutdownHook(new Thread(MainWithShutdown::onShutdown));
  }

  private static void onShutdown() {
    System.out.println("onShutdown");
  }

  public static void main(final String[] args) throws InterruptedException {
    int i = 0;
    while (5 > i++) {
      System.out.println("Sleep...");
      Thread.sleep(1000);
    }
  }

}
