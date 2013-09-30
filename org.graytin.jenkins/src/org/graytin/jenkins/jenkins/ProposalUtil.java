package org.graytin.jenkins.jenkins;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.fieldassist.IContentProposal;


/**
 * Filters criteria in pickers
 */
public class ProposalUtil {

	public static IContentProposal[] filterProposals(String contents, List<? extends IContentProposal> proposals) {
		List<IContentProposal> result = new ArrayList<IContentProposal>();
		for (IContentProposal proposalInstance : proposals) {
			String proposal = proposalInstance.getContent().toLowerCase();
			if (proposal.contains(contents.toLowerCase())) {
				result.add(proposalInstance);
			}
		}
		return (IContentProposal[]) result.toArray(new IContentProposal[result.size()]);
	}

}
