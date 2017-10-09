package edu.gatech.khet.listener;
import java.io.File;
import java.io.FileFilter;


public class KhetListenerFilter implements FileFilter {
  private String filter;

  public KhetListenerFilter() {
    this.filter = "";
  }

  public KhetListenerFilter(String filter) {
    this.filter = filter;
  }
  
  public boolean accept(File file) {
    if ("".equals(filter)) {
      return true;
    }
    return (file.getName().endsWith(filter));
  }
}