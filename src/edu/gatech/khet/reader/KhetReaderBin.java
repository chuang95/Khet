package edu.gatech.khet.reader;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;


public class KhetReaderBin implements KhetReader {
    
    private String filename;
    
    private HashMap<Long, Integer> kc;
    
    private int kSize;
    
    private int lowerThreshold;
    private int upperThreshold;
    
    public KhetReaderBin(String filename, int kSize, int upperThreshold)
    throws NullPointerException, IllegalArgumentException {
        this.filename = filename;
        this.kSize = kSize;
        //this.lowerThreshold = lowerThreshold;
        this.upperThreshold = upperThreshold;
        return;
    }
    
    @Override
    public boolean checkFile(String filename) {
        // TODO Auto-generated method stub
        System.out.println("check file "+filename);
        File f = new File(filename);
        return f.exists();
        
    }
    
    @SuppressWarnings("resource")
    @Override
    public void read(String filename) {
        // TODO Auto-generated method stub
        
        if(filename==null)
            return;
        
        if(checkFile(filename))
        {
            Path path = Paths.get("./"+filename);
            byte[] data = null;
            try {
                data = Files.readAllBytes(path);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            
            ByteBuffer wrapped = ByteBuffer.wrap(data); // big-endian by default
            int ul=data.length;
            //System.out.println(filename+" length: "+ul);
            data=null;
            
            kc = new HashMap<Long, Integer>();
            long kmer;
            int count;
            int length=0;
            
            long am = (~0) << (kSize);
            am = am << 1;
            long cm = (long) (Math.pow(2, kSize-1)-1);
            long bm = ~(am | cm);
            //System.out.println("mask "+Long.toBinaryString(am)+" "+Long.toBinaryString(bm)+" "+Long.toBinaryString(cm));
            //System.out.println(ul);
            
            while(length<ul)
            {
                length=length+12;
                kmer = wrapped.getLong(); // 1
                //System.out.print(Long.toBinaryString(kmer)+"\t");
                long ak = kmer & am;
                long bk = (kmer & bm) >> (kSize - 1);
                long ck = (kmer & cm) << 2;
                kmer = ak | bk | ck;
                count = wrapped.getInt();
                //				if(count < lowerThreshold || count > upperThreshold)
                //					continue;
                //System.out.println("changed: "+Long.toBinaryString(kmer));
                kc.put(kmer, count);
                
                //System.out.println(length);
            }
            //wrapped.clear();
            wrapped.flip();
            
            //sort
            Map<Long, Integer> map = new TreeMap<Long, Integer>(kc);
            //System.out.println("After Sorting:");
            kc.clear();
            
            //iterate map
            Iterator<Long> keySetIterator = map.keySet().iterator();
            while(keySetIterator.hasNext())
            {
                long key = keySetIterator.next();
                wrapped.putLong(key);
                wrapped.putInt(map.get(key));
                //System.out.println("key: " + key + " value: " + map.get(key));
            }
            map.clear();
            
            //writer
            OutputStream output = null;
            try {
                output = new BufferedOutputStream(new FileOutputStream(filename+".sort"));
                try {
                    //System.out.println("filezie: "+wrapped.array().length);
                    //System.out.println("dump sort file: "+filename);
                    output.write(wrapped.array());
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }      
            wrapped.clear();
            try {
                output.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return;
        }
        else
        {
            System.out.println("file " + filename + "does not exists");
            return;
        }
    }
    
    @Override
    public String getFilename() {
        // TODO Auto-generated method stub
        return filename;
    }
    
}
