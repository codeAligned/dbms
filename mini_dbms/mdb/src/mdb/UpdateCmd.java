// Automatically generated code.  Edit at your own risk!
// Generated by bali2jak v2002.09.03.

package mdb;

import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.LockMode;
import minidb.je.ExecuteHelpers;
import minidb.je.PredicateHelpers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static minidb.je.ExecuteHelpers.READ_WRITE;

public class UpdateCmd extends Update {

    final public static int ARG_LENGTH = 3 ;
    final public static int TOK_LENGTH = 4 ;

    public void execute () {
        
        super.execute();

//        MyDbEnv myDbEnv = new MyDbEnv();
        Database relationDB = null;
        Database updateDB = null;

        try {
//            myDbEnv.setup(ExecuteHelpers.myDbEnvPath, READ_WRITE);
            relationDB = ExecuteHelpers.myDbEnv.getDB("relationDB", READ_WRITE);
            StringBuilder relationMetaData = new StringBuilder();
            String relationName = getRel_name().toString();
            if(!ExecuteHelpers.isTablePresent(relationName, relationMetaData))
                System.err.println("\nRelation not present : " + relationName);

            Map<String, List<AstNode>> clauses = PredicateHelpers.generateClauses(relationName, getOne_rel_pred());
            List<AstNode> clausesList = clauses != null ? clauses.get(relationName) : null;
            List<String>[] data = ExecuteHelpers.getSelectData(new String(relationMetaData), clausesList);
            Map<String, String[]> metaColumnRelation = new HashMap<String, String[]>();
            Map<String, String[]> metaColumnTypeRelation = new HashMap<String, String[]>();
            Map<String, List<String[]>> allRowsOfRelations = new HashMap<String, List<String[]>>();

            PredicateHelpers.formatData(metaColumnRelation, metaColumnTypeRelation, allRowsOfRelations, data[0]);

            Map<String, List<AstNode>> assigns = PredicateHelpers.generateClauses(relationName, getAssign_list());

            int[] indices = PredicateHelpers.setIndices(metaColumnRelation, clauses, relationName);
            int[] assignIndices = PredicateHelpers.setIndices(metaColumnRelation, assigns, relationName);
            updateDB = ExecuteHelpers.myDbEnv.getDB(relationName+"DB", READ_WRITE);
            List<String> indexes = ExecuteHelpers.getAllIndexes(relationName);

            for(int j = 0; j < allRowsOfRelations.get(relationName).size(); j++) {
                String[] row = allRowsOfRelations.get(relationName).get(j);
                String oldRow[] = new String[row.length];
                for(int k = 0; k < oldRow.length; k++) oldRow[k] = row[k];
                boolean updateRow = PredicateHelpers.applyLocalPredicate(metaColumnTypeRelation.get(relationName), clauses, relationName, indices, row);

//                List<String> oldValues = new ArrayList<String>();
                if(updateRow)  {
                    for(int i = 0; i < assignIndices.length; i++) {
//                        oldValues.add(row[assignIndices[i]]);
                        row[assignIndices[i]] = assigns.get(relationName).get(i).arg[1].toString().trim().replaceAll(",", "&&");
                    }
                } else
                    continue;
                StringBuffer rowStr = new StringBuffer();
                for(int i = 0; i < row.length - 1; i++) rowStr.append(row[i]+",");
                rowStr.append(row[row.length-1]);
                DatabaseEntry theKey = new DatabaseEntry((data[1].get(j)).getBytes("UTF-8"));
                updateDB.delete(ExecuteHelpers.txn, theKey);

                theKey = new DatabaseEntry(((System.currentTimeMillis() / 1000L) + ":"+ rowStr.toString()).getBytes("UTF-8"));
                DatabaseEntry theData = new DatabaseEntry((rowStr).toString().getBytes("UTF-8"));
                updateDB.put(ExecuteHelpers.txn, theKey, theData);

                for(int i = 0; i < metaColumnRelation.get(relationName).length; i++) {
                    String indexToCheck = metaColumnRelation.get(relationName)[i];
//                for(int i = 0; i < assignIndices.length; i++) {
//                    //delete the old data and add new data to index if updated column has index.
//                    String indexToCheck = ExecuteHelpers.sanitizeColumn(assigns.get(relationName).get(i).arg[0].toString(), relationName);
                    Database indexDB = null;
                    if(indexes.contains(indexToCheck)) {
//                      System.out.println("Old: "+ oldValue + "is being replaced with :"+ row[assignIndices[i]]);
                        try{
                            indexDB = ExecuteHelpers.myDbEnv.getDB(indexToCheck + "DB", READ_WRITE);
                            //Remove old
                            DatabaseEntry tempData = new DatabaseEntry();
                            DatabaseEntry indexKey = new DatabaseEntry(ExecuteHelpers.bytify(oldRow[i]));
                            indexDB.get(ExecuteHelpers.txn, indexKey, tempData, LockMode.DEFAULT);
                            if(tempData.getSize() != 0) {
                                ByteArrayInputStream bais = new ByteArrayInputStream(tempData.getData());
                                DataInputStream in = new DataInputStream(bais);
                                ByteArrayOutputStream bOutput = new ByteArrayOutputStream();
                                DataOutputStream out = new DataOutputStream(bOutput);
                                while (in.available() > 0) {
                                    String storedData = in.readUTF();
                                    if(storedData.equals(data[1].get(j))) {
                                        if(oldRow[i].equals(row[i]))     //if both are equal, this column not changed.
                                            out.writeUTF(ExecuteHelpers.stringify(theKey));
                                    } else
                                        out.writeUTF(storedData);
                                }
                                theData = new DatabaseEntry(bOutput.toByteArray());
                                indexDB.put(ExecuteHelpers.txn, indexKey, theData);
                            }
                            //if both are equal, this column not changed.
                            //Key is updated earlier, just ignore.
                            if(oldRow[i].equals(row[i]))
                                continue;
                            //Add new
                            tempData = new DatabaseEntry();
                            indexKey = new DatabaseEntry(ExecuteHelpers.bytify(row[i]));
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
                            theData = new DatabaseEntry(bOutput.toByteArray());
                            indexDB.put(ExecuteHelpers.txn, indexKey, theData);
                        } finally {
                            if(indexDB != null) indexDB.close();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(relationDB != null) relationDB.close();
            if(updateDB != null) updateDB.close();
//            myDbEnv.close();
        }
    }

    public Assign_list getAssign_list () {
        
        return (Assign_list) arg [1] ;
    }

    public One_rel_pred getOne_rel_pred () {
        
        return (One_rel_pred) arg [2] ;
    }

    public Rel_name getRel_name () {
        
        return (Rel_name) arg [0] ;
    }

    public AstToken getSEMI () {
        
        return (AstToken) tok [3] ;
    }

    public AstToken getSET () {
        
        return (AstToken) tok [1] ;
    }

    public AstToken getUPDATE () {
        
        return (AstToken) tok [0] ;
    }

    public AstToken getWHERE () {
        
        return (AstToken) tok [2] ;
    }

    public boolean[] printorder () {
        
        return new boolean[] {true, false, true, false, true, false, true} ;
    }

    public UpdateCmd setParms
    (AstToken tok0, Rel_name arg0, AstToken tok1, Assign_list arg1, AstToken tok2, One_rel_pred arg2, AstToken tok3)
    {
        
        arg = new AstNode [ARG_LENGTH] ;
        tok = new AstTokenInterface [TOK_LENGTH] ;
        
        tok [0] = tok0 ;            /* UPDATE */
        arg [0] = arg0 ;            /* Rel_name */
        tok [1] = tok1 ;            /* SET */
        arg [1] = arg1 ;            /* Assign_list */
        tok [2] = tok2 ;            /* WHERE */
        arg [2] = arg2 ;            /* One_rel_pred */
        tok [3] = tok3 ;            /* SEMI */
        
        InitChildren () ;
        return (UpdateCmd) this ;
    }

}
