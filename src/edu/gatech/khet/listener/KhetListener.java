package edu.gatech.khet.listener;
import java.util.*;
import java.io.*;

public abstract class KhetListener extends TimerTask{
  private String path;
  private File filesArray [];
  private HashMap<File, Long> dir = new HashMap<File, Long>();
  private KhetListenerFilter dfw;

  public KhetListener(String path) {
    this(path, "");
  }

  public KhetListener(String path, String filter) {
    this.path = path;
    dfw = new KhetListenerFilter(filter);
    filesArray = new File(path).listFiles(dfw);

    // transfer to the hashmap be used a reference and keep the
    // lastModfied value
    for(int i = 0; i < filesArray.length; i++) {
       dir.put(filesArray[i], new Long(filesArray[i].lastModified()));
    }
  }

  @SuppressWarnings("unchecked")
public final void run() {
    HashSet<File> checkedFiles = new HashSet<File>();
    filesArray = new File(path).listFiles(dfw);

    // scan the files and check for modification/addition
    for(int i = 0; i < filesArray.length; i++) {
      Long current = dir.get(filesArray[i]);
      checkedFiles.add(filesArray[i]);
      if (current == null) {
        // new file
        dir.put(filesArray[i], new Long(filesArray[i].lastModified()));
        onChange(filesArray[i], "add");
      }
//      else if (current.longValue() != filesArray[i].lastModified()){
//        // modified file
//        dir.put(filesArray[i], new Long(filesArray[i].lastModified()));
//        onChange(filesArray[i], "modify");
//      }
    }

    // now check for deleted files
    Set<File> ref = ((HashMap<File, Long>)dir.clone()).keySet();
    ref.removeAll((Set<File>)checkedFiles);
    Iterator<File> it = ref.iterator();
    while (it.hasNext()) {
      File deletedFile = it.next();
      dir.remove(deletedFile);
      onChange(deletedFile, "delete");
    }
  }

  protected abstract void onChange( File file, String action );
}
