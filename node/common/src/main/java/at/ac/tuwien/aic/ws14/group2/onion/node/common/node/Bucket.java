package at.ac.tuwien.aic.ws14.group2.onion.node.common.node;

import java.util.Arrays;

/**
 * Created by klaus on 12/2/14.
 */
public class Bucket implements Comparable<Bucket> {
	private final byte[] data;
	private final short nr;

	Bucket(byte[] data, short sequenceNumber) {
		this.data = data;
		this.nr = sequenceNumber;
	}

	public byte[] getData() {
		return data;
	}

	public short getNr() {
		return nr;
	}

	@Override
	public String toString() {
		return "Bucket{" +
				"data=" + Arrays.toString(data) +
				", nr=" + nr +
				'}';
	}

	@Override
	public int compareTo(Bucket bucket) {
		return Short.compare(this.nr, bucket.nr);
	}
}
