// Automatically generated code.  Edit at your own risk!
// Generated by bali2jak v2002.09.03.

package mdb;

import com.sleepycat.je.EnvironmentNotFoundException;
import minidb.je.ExecuteHelpers;

import java.util.ArrayList;

public class ShowDb extends Show {

    final public static int ARG_LENGTH = 1 /* Kludge! */ ;
    final public static int TOK_LENGTH = 2 ;

//    public static boolean findRelation(String relationName) {
//        return ExecuteHelpers.showAllRowsOfTable("relationDB", relationName, 0);
//    }

//    private static String showContentsOfAllRelations()
//            throws DatabaseException {
//        ArrayList<String> relations = ExecuteHelpers.getAllRowsOfTable("relationDB");
//        StringBuffer displayString = new StringBuffer();
//        for(int i = 0; i < relations.size(); i++) {
//            String relationName = relations.get(i);
//            displayString.append(ExecuteHelpers.getSelectData(relationName));
//            displayString.append("\n");
//        }
//        return displayString.toString();
//    }


    public void execute () {
        
        super.execute();

        try {
            System.out.println(showDescOfAllRelations());
        } catch(EnvironmentNotFoundException e) {
            System.err.println("Database is currently empty!!.");
            return;
        }
    }

    private String showDescOfAllRelations() {
        ArrayList<String> relationDBcontent = ExecuteHelpers.getAllRowsOfTable("relationDB")[0];
        StringBuffer contents = new StringBuffer();
        System.out.println();
        for(String desc : relationDBcontent) {
            renderDescOfSingleRelation(contents, desc);
        }
        return contents.toString();
    }

    static void renderDescOfSingleRelation(StringBuffer contents, String desc) {
        String[] splitDesc = desc.split(",");
        contents.append("Relation:"+ splitDesc[0] + "\n");
        if(splitDesc.length > 1) {
            contents.append("Field, Type\n");
            for(int i = 1; i < splitDesc.length; i++) {
                String[] splitField = splitDesc[i].split(":");
                contents.append(splitField[0] + ","+ splitField[1]+"\n");
            }
        }
        contents.append("\n");
    }

    public AstToken getSEMI () {
        
        return (AstToken) tok [1] ;
    }

    public AstToken getSHOW () {
        
        return (AstToken) tok [0] ;
    }

    public boolean[] printorder () {
        
        return new boolean[] {true, true} ;
    }

    public ShowDb setParms (AstToken tok0, AstToken tok1) {
        
        arg = new AstNode [ARG_LENGTH] ;
        tok = new AstTokenInterface [TOK_LENGTH] ;
        
        tok [0] = tok0 ;            /* SHOW */
        tok [1] = tok1 ;            /* SEMI */
        
        InitChildren () ;
        return (ShowDb) this ;
    }

}
