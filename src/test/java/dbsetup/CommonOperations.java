package dbsetup;

import com.ninja_squad.dbsetup.operation.Operation;
import static com.ninja_squad.dbsetup.Operations.deleteAllFrom;

public class CommonOperations {
	public static final Operation DELETE_ALL = deleteAllFrom("BOOK");
}
