package org.eclipse.help.internal.ui.search;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import java.util.*;
import java.util.List;
import org.eclipse.ui.*;
import org.eclipse.jface.resource.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.custom.*;
import org.eclipse.help.internal.contributions.*;
import org.eclipse.help.internal.HelpSystem;
import org.eclipse.help.internal.ui.util.*;

/**
 * Displays Search Filtering Options dialog.
 */
public class SearchFilteringOptions {
	
	private Composite control = null;
	private CheckboxTreeViewer checkboxTreeViewer;
	private HelpSearchQuery query;
	private Collection excludedCategories; // = query.getExcludedCategories()


	public SearchFilteringOptions(Composite parent, HelpSearchQuery query) {
		this.query = query;
		excludedCategories = query.getExcludedCategories();
		if (excludedCategories == null)
			excludedCategories = new ArrayList();
		createControl(parent);
	}
	/**
	 * Fills in the dialog area with text and checkboxes
	 * @param the parent composite to contain the dialog area
	 * @return the dialog area control
	 */
	protected Control createControl(Composite parent) {
		//Group group = new Group((Composite) parent, SWT.SHADOW_NONE);
		Composite group = new Composite(parent, SWT.SHADOW_NONE);
		//group.setText(WorkbenchResources.getString("Search_only_within"));
		group.setLayout(new GridLayout());
		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		data.horizontalSpan = 2;
		data.heightHint = 200;
		data.widthHint = 200;
		group.setLayoutData(data);

		control = group;

		InfoSet infoset = HelpSystem.getNavigationManager().getCurrentInfoSet();
		checkboxTreeViewer =
			new CheckboxTreeViewer(control, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);

		// Listen to check state changes
		checkboxTreeViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				if (event.getElement() instanceof InfoView)
					checkboxTreeViewer.setSubtreeChecked(event.getElement(), event.getChecked());
				else if (event.getElement() instanceof Topic){
					if(((Topic)event.getElement()).getParent() instanceof InfoView)
						updateInfoViewSelection((InfoView)((Topic)event.getElement()).getParent());
				}
				
			}
		});
	
		checkboxTreeViewer.setContentProvider(CheckboxTreeContentProvider.getDefault());
		checkboxTreeViewer.setLabelProvider(CheckboxLabelProvider.getDefault());
		checkboxTreeViewer.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));
		checkboxTreeViewer.setInput(infoset);
		checkboxTreeViewer.setExpandedElements(infoset.getChildrenList().toArray());
		
		setExcludedCategories(query.getExcludedCategories());
		checkboxTreeViewer.refresh();
		return control;
	}
	public Control getControl() {
		return control;
	}
	/**
	 * Returns a list of Topics (categories) that are to be excluded from search
	 */
	public List getExcludedCategories()
	{
		InfoSet infoset = HelpSystem.getNavigationManager().getCurrentInfoSet();
		ArrayList categories = new ArrayList();
		for (Iterator viewIterator = infoset.getChildren(); viewIterator.hasNext();	) {
			InfoView view = (InfoView) viewIterator.next();
			for (Iterator topicIterator = view.getChildren(); topicIterator.hasNext();) {
				categories.add(topicIterator.next());
			}
		}
			
		Object[] checkedElements = checkboxTreeViewer.getCheckedElements();
		for (int i=0; i<checkedElements.length; i++)
		{
			Contribution c = (Contribution)checkedElements[i];
			categories.remove(c);
		}
		return categories;
	}
	public void setEnabled(boolean enabled) {
		control.setEnabled(enabled);
		Control[] children=control.getChildren();
		for (int i=0; i<children.length; i++)
			children[i].setEnabled(enabled);
	}
	/**
	 * Selects checkboxes based on (categories) that are to be excluded from search
	 */
	public void setExcludedCategories(List categories)
	{
		InfoSet infoset = HelpSystem.getNavigationManager().getCurrentInfoSet();
		
		for (Iterator viewIterator = infoset.getChildren(); viewIterator.hasNext();	) {
			InfoView view = (InfoView) viewIterator.next();
			// First set all, then un-check
			checkboxTreeViewer.setSubtreeChecked(view,true);
			for (Iterator topicIterator = view.getChildren(); topicIterator.hasNext();) {
				Contribution topic = (Contribution)topicIterator.next();
				if (categories !=null && categories.contains(topic))
					checkboxTreeViewer.setChecked(topic, false);
			}
			updateInfoViewSelection(view);
		}
	}
	/**
	 * Selects InfoView if at least one of children topics is selected
	 * Deselects InfoView if none of children topics is selected
	 */
	private void updateInfoViewSelection(InfoView view){
		boolean viewSelected=false;
		for (Iterator topicIterator = view.getChildren(); topicIterator.hasNext();) {
			Contribution topic = (Contribution)topicIterator.next();
			if (checkboxTreeViewer.getChecked(topic)){
				viewSelected=true;
				break;
			}
		}
		if(checkboxTreeViewer.getChecked(view)!=viewSelected)
			checkboxTreeViewer.setChecked(view, viewSelected);
	}
}
