// Automatically generated code.  Edit at your own risk!
// Generated by bali2jak v2002.09.03.

package mdb;

public class Literal_listElem extends AstListNode {

    public AstToken getCOMMA () {
        return (AstToken) tok [0] ;
    }

    public Literal getLiteral () {
        
        return (Literal) arg [0] ;
    }

    public Literal_listElem setParms (AstToken tok0, Literal arg0) {
        
        tok = new AstToken [1] ;
        tok [0] = tok0 ;            /* COMMA */
        return setParms (arg0) ;    /* Literal */
    }

    public Literal_listElem setParms (Literal arg0) {
        
        super.setParms (arg0) ;     /* Literal */
        return (Literal_listElem) this ;
    }

}