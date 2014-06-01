// Automatically generated code.  Edit at your own risk!
// Generated by bali2jak v2002.09.03.

package mdb;

import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.LockMode;
import minidb.je.ExecuteHelpers;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static minidb.je.ExecuteHelpers.READ_WRITE;

public class InsertCmd extends Insert {

    final public static int ARG_LENGTH = 2 ;
    final public static int TOK_LENGTH = 6 ;

    public void execute () {

        super.execute();
        String relName = getRel_name().toString().trim();

//        MyDbEnv myDbEnv = new MyDbEnv();
//        myDbEnv.setup(ExecuteHelpers.myDbEnvPath, READ_WRITE);
//        Database relationDB = ExecuteHelpers.myDbEnv.getDB("relationDB", READ_WRITE);

        StringBuilder relMetaData = new StringBuilder();
        if(!ExecuteHelpers.isTablePresent(relName, relMetaData)) {
            System.err.println(relName + " is not created. Please first create it! :|");
            return;
        }
//        relationDB.close();
        String[] columns = new String[0];
//        try {
            columns = relMetaData.toString().split(",");
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }

        StringBuffer dataString = new StringBuffer();
        AstCursor c = new AstCursor();
        List<String> row = new ArrayList<String>();
        for (c.FirstElement(getLiteral_list()); c.MoreElement(); c.NextElement()) {
            String data = c.node.toString().trim().replaceAll(",", "&&");
            dataString.append(data+",");
            row.add(data);
        }
        dataString = dataString.deleteCharAt(dataString.length()-1);

        if(row.size() != (columns.length-1)) {       //Skip first element: tableName
            System.err.println("Inserted values Count("+row.size()+") doesn't match the table schema! ("+(columns.length-1)+")");
            return;
        }
        Database insertDB = null;
        try {
            DatabaseEntry theKey = new DatabaseEntry(((System.currentTimeMillis() / 1000L) + ":"+ dataString.toString()).getBytes("UTF-8"));
            DatabaseEntry theData = new DatabaseEntry(dataString.toString().getBytes("UTF-8"));

            insertDB = ExecuteHelpers.myDbEnv.getDB(relName + "DB", READ_WRITE);
            insertDB.put(ExecuteHelpers.txn, theKey, theData);
            List<String> indexes = ExecuteHelpers.getAllIndexes(relName);

            for(int i = 1; i < columns.length; i++) {
                String column = columns[i].split(":")[0];
                String relPlusColumnName = relName + "." + column;
                Database indexDB = null;
                if(indexes.contains(relPlusColumnName)) {
                    try{
                        indexDB = ExecuteHelpers.myDbEnv.getDB(relPlusColumnName + "DB", READ_WRITE);
                        //Remove old
                        DatabaseEntry tempData = new DatabaseEntry();
                        DatabaseEntry indexKey = new DatabaseEntry(row.get(i-1).getBytes("UTF-8")); // row[i] is value
                        ByteArrayOutputStream bOutput = new ByteArrayOutputStream();
                        DataOutputStream out = new DataOutputStream(bOutput);
                        indexDB.get(ExecuteHelpers.txn, indexKey, tempData, LockMode.DEFAULT);
                        if(tempData.getSize() != 0) {
                            ByteArrayInputStream bais = new ByteArrayInputStream(tempData.getData());
                            DataInputStream in = new DataInputStream(bais);
                            while (in.available() > 0)
                                out.writeUTF(in.readUTF());
                        }
                        out.writeUTF(ExecuteHelpers.stringify(theKey));
                        indexDB.put(ExecuteHelpers.txn, indexKey, new DatabaseEntry(bOutput.toByteArray()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if(indexDB != null) indexDB.close();
                    }
                }
            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } finally {
            if(insertDB != null) insertDB.close();
//            myDbEnv.close();
        }

    }

    public AstToken getINSERT () {
        
        return (AstToken) tok [0] ;
    }

    public AstToken getINTO () {
        
        return (AstToken) tok [1] ;
    }

    public AstToken getLP () {
        
        return (AstToken) tok [3] ;
    }

    public Literal_list getLiteral_list () {
        
        return (Literal_list) arg [1] ;
    }

    public AstToken getRP () {
        
        return (AstToken) tok [4] ;
    }

    public Rel_name getRel_name () {
        
        return (Rel_name) arg [0] ;
    }

    public AstToken getSEMI () {
        
        return (AstToken) tok [5] ;
    }

    public AstToken getVALUES () {
        
        return (AstToken) tok [2] ;
    }

    public boolean[] printorder () {
        
        return new boolean[] {true, true, false, true, true, false, true, true} ;
    }

    public InsertCmd setParms
    (AstToken tok0, AstToken tok1, Rel_name arg0, AstToken tok2, AstToken tok3, Literal_list arg1, AstToken tok4, AstToken tok5)
    {
        
        arg = new AstNode [ARG_LENGTH] ;
        tok = new AstTokenInterface [TOK_LENGTH] ;
        
        tok [0] = tok0 ;            /* INSERT */
        tok [1] = tok1 ;            /* INTO */
        arg [0] = arg0 ;            /* Rel_name */
        tok [2] = tok2 ;            /* VALUES */
        tok [3] = tok3 ;            /* LP */
        arg [1] = arg1 ;            /* Literal_list */
        tok [4] = tok4 ;            /* RP */
        tok [5] = tok5 ;            /* SEMI */
        
        InitChildren () ;
        return (InsertCmd) this ;
    }

}