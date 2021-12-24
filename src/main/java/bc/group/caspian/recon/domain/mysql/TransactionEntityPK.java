package bc.group.caspian.recon.domain.mysql;

import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@NoArgsConstructor
public class TransactionEntityPK implements Serializable {

    private String id;
    private String cashEntry;

    public TransactionEntityPK(String id, String cashEntry) {
        this.id = id;
        this.cashEntry = cashEntry;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof TransactionEntityPK))
            return false;
        TransactionEntityPK other = (TransactionEntityPK) o;
        boolean idEquals = (this.id == null && other.id == null)
                || (this.id != null && this.id.equals(other.id));
        boolean cashEntryEquals = (this.cashEntry == null && other.cashEntry == null)
                || (this.cashEntry != null && this.cashEntry.equals(other.cashEntry));
        return idEquals && cashEntryEquals;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, cashEntry);
    }
}
