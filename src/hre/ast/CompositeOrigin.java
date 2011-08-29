// -*- tab-width:2 ; indent-tabs-mode:nil -*-
package hre.ast;


import java.util.Arrays;

public class CompositeOrigin implements Origin {

    private Origin origins[];
    public CompositeOrigin(Origin ... origins){
        this.origins=Arrays.copyOf(origins,origins.length);
    }
    public String toString(){
      String result=origins[0].toString();
      for(int i=1;i<origins.length;i++){
        result+=" and "+origins[i].toString();
      }
      return result;
    }

}

