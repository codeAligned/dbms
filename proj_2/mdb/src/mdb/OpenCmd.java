// Automatically generated code.  Edit at your own risk!
// Generated by bali2jak v2002.09.03.

package mdb;

import minidb.je.ExecuteHelpers;

import java.io.File;

import static minidb.je.ExecuteHelpers.myDbEnvPath;

public class OpenCmd extends Open {

    final public static int ARG_LENGTH = 1 /* Kludge! */ ;
    final public static int TOK_LENGTH = 3 ;

    public void execute () {
        
        super.execute();
        myDbEnvPath = new File(getSTRING_LITERAL().getTokenName().trim().replaceAll("^\"|\"$", ""));
        if(!myDbEnvPath.exists())
            myDbEnvPath.mkdirs(); //make directory if it doesn't exist yet
        ExecuteHelpers.prepareDB();
    }

    public AstToken getOPEN () {
        
        return (AstToken) tok [0] ;
    }

    public AstToken getSEMI () {
        
        return (AstToken) tok [2] ;
    }

    public AstToken getSTRING_LITERAL () {
        
        return (AstToken) tok [1] ;
    }

    public boolean[] printorder () {
        
        return new boolean[] {true, true, true} ;
    }

    public OpenCmd setParms (AstToken tok0, AstToken tok1, AstToken tok2) {
        
        arg = new AstNode [ARG_LENGTH] ;
        tok = new AstTokenInterface [TOK_LENGTH] ;
        
        tok [0] = tok0 ;            /* OPEN */
        tok [1] = tok1 ;            /* STRING_LITERAL */
        tok [2] = tok2 ;            /* SEMI */
        
        InitChildren () ;
        return (OpenCmd) this ;
    }

}
