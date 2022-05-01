/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pianomix;

import static pianomix.PianoMix.share;

/**
 *
 * @author Egor
 */
public class Note {
    
    int x;
    int y;
    
    int length;
    

    public Note(int x, int y) {
        this.x = x;
        this.y = y;
        if(share.lastLength==0){
            this.length=1;
        }else{
        this.length= share.lastLength;
        }
    }
    public Note(int x, int y,int lenght) {
        this.x = x;
        this.y = y;
        this.length=lenght;
    }
    
    
    
}
