package org.eclipse.core.internal.indexing;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

abstract class IndexedStoreObject extends StoredObject {

	public IndexedStoreObject() {
		super();
	}
	
	/**
	 * Constructs an object from bytes that came from the store.
	 * These bytes include the 2 byte type field.
	 */
	public IndexedStoreObject(Field f, ObjectStore store, ObjectAddress address) throws ObjectStoreException {
		super(f, store, address);
	}
	
	/**
	 * Acquires an anchor.
	 */
	protected final IndexAnchor acquireAnchor(ObjectAddress address) throws IndexedStoreException {
		return (IndexAnchor) acquireObject(address);
	}
	
	/**
	 * Acquires a node.
	 */
	protected final IndexNode acquireNode(ObjectAddress address) throws IndexedStoreException {
		return (IndexNode) acquireObject(address);
	}
	
	/**
	 * Acquires an object.
	 */
	protected final StoredObject acquireObject(ObjectAddress address) throws IndexedStoreException {
		StoredObject object;
		try {
			object = store.acquireObject(address);
		} catch (ObjectStoreException e) {
			throw new IndexedStoreException(IndexedStoreException.ObjectNotAcquired);
		}
		return object;
	}
	
	/** 
	 * Inserts a new object into my store. Subclasses must not override.
	 */
	protected final ObjectAddress insertObject(StoredObject object) throws IndexedStoreException {
		try {
			ObjectAddress address = store.insertObject(object);
			return address;
		} catch (ObjectStoreException e) {
			throw new IndexedStoreException(IndexedStoreException.ObjectNotStored);
		}
	}
	
	/**
	 * Releases this object.  Subclasses must not override.
	 */
	protected final void release() throws IndexedStoreException {
		try {
			store.releaseObject(this);
		} catch (ObjectStoreException e) {
			throw new IndexedStoreException(IndexedStoreException.ObjectNotReleased);
		}
	}
	
	/** 
	 * Removes an object from my store.  Subclasses must not override.
	 */
	protected final void removeObject(ObjectAddress address) throws IndexedStoreException {
		try {
			store.removeObject(address);
		} catch (ObjectStoreException e) {
			throw new IndexedStoreException(IndexedStoreException.ObjectNotRemoved);
		}
	}
}
