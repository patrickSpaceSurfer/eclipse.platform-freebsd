package org.eclipse.update.core;

import java.io.IOException;
import java.io.InputStream;

import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public abstract class AbstractSite implements ISite {

	

	private ListenersList listeners = new ListenersList();
	private URL siteURL;
	private List features;

	/**
	 * Constructor for AbstractSite
	 */
	public AbstractSite(URL siteReference) {
		super();
		this.siteURL = siteReference;
	}
	
	/**
	 * @see ISite#addSiteChangedListener(ISiteChangedListener)
	 */
	public void addSiteChangedListener(ISiteChangedListener listener) {
		synchronized (listeners){
			listeners.add(listener);
		}
	}
	
	/**
	 * @see ISite#removeSiteChangedListener(ISiteChangedListener)
	 */
	public void removeSiteChangedListener(ISiteChangedListener listener) {
		synchronized (listeners){
			listeners.remove(listener);
		}
	}	

	/**
	 * @see ISite#install(IFeature, IProgressMonitor)
	 */
	public void install(IFeature sourceFeature, IProgressMonitor monitor) throws CoreException {
		// should start UOW and manage Progress Monitor
		AbstractFeature localFeature = createExecutableFeature(sourceFeature);
		sourceFeature.install(localFeature);
		this.addFeature(localFeature);
	}
	
	/**
	 * @see ISite#remove(IFeature, IProgressMonitor)
	 */
	public void remove(IFeature feature, IProgressMonitor monitor)
		throws CoreException {
	}	

	/**
	 * 
	 */
	public abstract AbstractFeature createExecutableFeature(IFeature sourceFeature);

	/**
	 *
	 */
	public abstract InputStream getInputStream(
		IFeature sourceFeature,
		String streamKey);

	/**
	 * Gets the siteURL
	 * @return Returns a URL
	 */
	public URL getURL() {
		return siteURL;
	}

	/**
	* This method also closes both streams.
	* Taken from FileSystemStore
	*/
	protected void transferStreams(InputStream source, OutputStream destination)
		throws IOException {
		try {
			byte[] buffer = new byte[8192];
			while (true) {
				int bytesRead = source.read(buffer);
				if (bytesRead == -1)
					break;
				destination.write(buffer, 0, bytesRead);
			}
		} finally {
			try {
				source.close();
			} catch (IOException e) {
			}
			try {
				destination.close();
			} catch (IOException e) {
			}
		}
	}
	/**
	 * Gets the features
	 * @return Returns a IFeature[]
	 */
	public IFeature[] getFeatures() {
		return (IFeature[])features.toArray();
	}
	
	/**
	 * adds a feature
	 * @param feature The feature to add
	 */
	private void addFeature(IFeature feature) {
		if (features==null){
			features = new ArrayList(0);
		}
		this.features.add(feature);
	}

}