package com.larseckart;

import com.larseckart.adapters.cli.CliApplication;
import com.larseckart.adapters.web.WebApplication;

public class App {

  public static void main(String[] args) {
    String mode = System.getProperty("app.mode", "cli");

    if ("web".equalsIgnoreCase(mode)) {
      WebApplication.main(args);
    } else {
      CliApplication.main(args);
    }
  }
}
