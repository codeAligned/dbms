// Automatically generated code.  Edit at your own risk!
// Generated by bali2jak v2002.09.03.

package mdb;

public class PredElem extends AstListNode {

    public AstToken getAND () {
        return (AstToken) tok [0] ;
    }

    public Clause getClause () {
        
        return (Clause) arg [0] ;
    }

    public PredElem setParms (AstToken tok0, Clause arg0) {
        
        tok = new AstToken [1] ;
        tok [0] = tok0 ;            /* AND */
        return setParms (arg0) ;    /* Clause */
    }

    public PredElem setParms (Clause arg0) {
        
        super.setParms (arg0) ;     /* Clause */
        return (PredElem) this ;
    }

}