package org.graytin.subversive;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.BaseMessages;
import org.eclipse.team.svn.core.SVNMessages;
import org.eclipse.team.svn.core.connector.SVNConnectorException;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.operation.IUnprotectedOperation;
import org.eclipse.team.svn.core.operation.remote.AbstractRepositoryOperation;
import org.eclipse.team.svn.core.resource.IRepositoryContainer;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IRepositoryResourceProvider;
import org.eclipse.team.svn.core.utility.ProgressMonitorUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.graytin.jenkins.update.SDKActivator;
import org.graytin.jenkins.update.SDKBundleUpdatePreferencePage;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * Locate Eclipse IDE projects on the repository
 * 
 */
public class SearchProjectOperation extends AbstractRepositoryOperation implements IRepositoryResourceProvider {
	private static boolean invalidate;

	private LoadingCache<String, List<IRepositoryResource>> cache;

	private int depthOfNestedFoldersScan = 1;

	public SearchProjectOperation(IRepositoryResource[] startFrom) {
		super("Operation_LocateProjects", SVNMessages.class, startFrom); //$NON-NLS-1$
	}

	/**
	 * Get all projects
	 */
	public IRepositoryResource[] getRepositoryResources() {
		List<IRepositoryResource> result = getRepositoryResourcesAsList();
		return result.toArray(new IRepositoryResource[result.size()]);
	}

	/**
	 * Get all projects
	 */
	public List<IRepositoryResource> getRepositoryResourcesAsList() {
		try {

			return cache.get(toString());
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return Collections.emptyList();
	}

	protected void runImpl(final IProgressMonitor monitor) throws Exception {
		try {
			if (cache == null) {
				cache = CacheBuilder.newBuilder()

				.maximumSize(50)

				.expireAfterAccess(15, TimeUnit.MINUTES)

				.build(new CacheLoader<String, List<IRepositoryResource>>() {
					@Override
					public List<IRepositoryResource> load(String key) throws Exception {
						SearchProjectOperation.this.depthOfNestedFoldersScan = SDKActivator.getDefault().getPreferenceStore()
								.getInt(SDKBundleUpdatePreferencePage.DEPTH_OF_SVN_SCAN);
						List<IRepositoryResource> found = resolveProjects(monitor);
						return found;
					}
				});
			}
			if (invalidate) {
				cache.invalidateAll();
			}
		} finally {
			monitor.done();
		}

	}

	private ArrayList<IRepositoryResource> resolveProjects(IProgressMonitor monitor) throws Exception {
		IRepositoryResource[] baseFolders = SVNUtility.shrinkChildNodes(this.operableData());
		for (int i = 0; i < baseFolders.length; i++) {
			SVNRevision selectedRevision = baseFolders[i].getSelectedRevision();
			SVNRevision pegRevision = baseFolders[i].getPegRevision();
			baseFolders[i] = baseFolders[i].getRepositoryLocation().asRepositoryContainer(baseFolders[i].getUrl(), false);
			baseFolders[i].setSelectedRevision(selectedRevision);
			baseFolders[i].setPegRevision(pegRevision);
		}
		ArrayList<IRepositoryResource> found = new ArrayList<IRepositoryResource>();
		this.findProjects(monitor, found, baseFolders, 0);
		return found;
	}

	protected void findProjects(final IProgressMonitor monitor, final List<IRepositoryResource> found, IRepositoryResource[] baseFolders, final int level)
			throws Exception {
		if (level > depthOfNestedFoldersScan) {
			return;
		}
		for (int i = 0; i < baseFolders.length && !monitor.isCanceled(); i++) {
			final IRepositoryResource current = baseFolders[i];
			if (current instanceof IRepositoryContainer) {
				protectStep(new IUnprotectedOperation() {
					public void run(IProgressMonitor monitor) throws Exception {
						String message = getOperationResource("Scanning"); //$NON-NLS-1$
						ProgressMonitorUtility.setTaskInfo(monitor, SearchProjectOperation.this,
								BaseMessages.format(message, new Object[] { current.getUrl() }));
						final IRepositoryResource[] children = ((IRepositoryContainer) current).getChildren();

						if (monitor.isCanceled())
							return;
						/*
						 * Set peg revision for children This is needed in following case: we're looking for projects in specified revision and traverse
						 * children for resource. If child exists in specified revision but doesn't in HEAD revision, then we need to specify its peg revision
						 * because 'getChildren' operation for children doesn't set peg revision (it means that it equals to HEAD) and it fill cause error.
						 */
						for (IRepositoryResource child : children) {
							child.setPegRevision(current.getSelectedRevision());
							if (child instanceof IRepositoryContainer) {
								found.add(child);
							}
						}
						findProjects(monitor, found, children, level + 1);
					}
				}, monitor, baseFolders.length);
			}
		}
	}

	public static void markCacheAsInvalid() {
		invalidate = true;
	}

	@Override
	public String toString() {
		IRepositoryResource resource = operableData()[0];
		String cacheName = "error";
		try {
			cacheName = resource.getUrl() + "#" + resource.getRevision();
		} catch (SVNConnectorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return cacheName;
	}
}
