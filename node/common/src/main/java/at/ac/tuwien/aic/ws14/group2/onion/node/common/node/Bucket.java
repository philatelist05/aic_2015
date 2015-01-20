package at.ac.tuwien.aic.ws14.group2.onion.node.common.node;

import java.util.Arrays;

/**
 * Created by klaus on 12/2/14.
 */
public class Bucket implements Comparable<Bucket> {
	private final byte[] data;
	private final long nr;

	public Bucket(byte[] data, long sequenceNumber) {
		this.data = data;
		this.nr = sequenceNumber;
	}

	public byte[] getData() {
		return data;
	}

	public long getNr() {
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
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Bucket bucket = (Bucket) o;

		if (nr != bucket.nr) return false;

		return true;
	}

	@Override
	public int hashCode() {
		return (int) nr;
	}

	@Override
	public int compareTo(Bucket bucket) {
		return Long.compare(this.nr, bucket.nr);
	}
}
