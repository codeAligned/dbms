// Automatically generated code.  Edit at your own risk!
// Generated by bali2jak v2002.09.03.

package mdb;
import Jakarta.util.*;
import java.io.*;
import java.util.*;

public class IndxDecl extends Decl_ind {

    final public static int ARG_LENGTH = 1 ;
    final public static int TOK_LENGTH = 2 ;

    public void execute () {
        
        super.execute();
    }

    public AstToken getINDEX () {
        
        return (AstToken) tok [0] ;
    }

    public Rel_dot_field getRel_dot_field () {
        
        return (Rel_dot_field) arg [0] ;
    }

    public AstToken getSEMI () {
        
        return (AstToken) tok [1] ;
    }

    public boolean[] printorder () {
        
        return new boolean[] {true, false, true} ;
    }

    public IndxDecl setParms (AstToken tok0, Rel_dot_field arg0, AstToken tok1) {
        
        arg = new AstNode [ARG_LENGTH] ;
        tok = new AstTokenInterface [TOK_LENGTH] ;
        
        tok [0] = tok0 ;            /* INDEX */
        arg [0] = arg0 ;            /* Rel_dot_field */
        tok [1] = tok1 ;            /* SEMI */
        
        InitChildren () ;
        return (IndxDecl) this ;
    }

}
