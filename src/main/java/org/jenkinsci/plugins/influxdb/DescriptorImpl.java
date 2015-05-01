package org.jenkinsci.plugins.influxdb;

import hudson.model.AbstractProject;
import hudson.model.ModelObject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Publisher;
import hudson.util.CopyOnWriteList;
import hudson.util.CopyOnWriteMap;
import hudson.util.FormValidation;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import java.util.Iterator;

/**
 *
 * @author jrajala-eficode
 * @author joachimrodrigues
 *
 * 
 */
public final class DescriptorImpl extends BuildStepDescriptor<Publisher> implements ModelObject {

	private final CopyOnWriteList<Server> servers = new CopyOnWriteList<Server>();

	private InfluxDbValidator validator = new InfluxDbValidator();

	/**
	 * The default constructor.
	 */
	public DescriptorImpl() {
		super(InfluxDbPublisher.class);
		load();
	}

	/**
	 * @return servers
	 */
	public Server[] getServers() {
		Iterator<Server> it = servers.iterator();
		int size = 0;
		while (it.hasNext()) {
			it.next();
			size++;
		}
		return servers.toArray(new Server[size]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see hudson.model.Descriptor#getDisplayName()
	 */
	@Override
	public String getDisplayName() {
		return "Publish metrics to InfluxDb Server";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see hudson.tasks.BuildStepDescriptor#isApplicable(java.lang.Class)
	 */
	@Override
	public boolean isApplicable(Class<? extends AbstractProject> jobType) {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see hudson.model.Descriptor#newInstance(org.kohsuke.stapler.StaplerRequest, net.sf.json.JSONObject)
	 */
	@Override
	public Publisher newInstance(StaplerRequest req, JSONObject formData) {
		InfluxDbPublisher publisher = new InfluxDbPublisher();
		req.bindParameters(publisher, "publisherBinding.");
		return publisher;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see hudson.model.Descriptor#configure(org.kohsuke.stapler.StaplerRequest, net.sf.json.JSONObject)
	 */
	@Override
	public boolean configure(StaplerRequest req, JSONObject formData) {
		servers.replaceBy(req.bindParametersToList(Server.class, "serverBinding."));
		//databaseName = formData.optString("databaseName", "");
		save();
		return true;
	}



	public InfluxDbValidator getValidator() {
	return validator;
}


public void setValidator(InfluxDbValidator validator) {
	this.validator = validator;
}






	/**
	 * @param ip
	 * @param port
	 * @return form validation of connection status.
	 */
	public FormValidation doTestConnection(@QueryParameter("serverBinding.host") final String ip,
			@QueryParameter("serverBinding.port") final String port,
			@QueryParameter("serverBinding.protocol") final String protocol) {
		if(protocol.equals("UDP")) {
			return FormValidation.ok("UDP is configured");
		}
		else if(protocol.equals("TCP")) {
			if (!validator.isHostPresent(ip) || !validator.isPortPresent(port)
					|| !validator.isListening(ip, Integer.parseInt(port))) {
				return FormValidation.error("Server is not listening... Or host:port are not correctly filled");
			}

			return FormValidation.ok("Server is listening");
		} else {
			return FormValidation.ok("Unknown protocol");
		}
	}

	/**
	 * @param value
	 * @return  form validation of host status.
	 */
	public FormValidation doCheckHost(@QueryParameter final String value) {
		if (!validator.isHostPresent(value)) {
			return FormValidation.error("Please set a hostname");
		}


		return FormValidation.ok("Hostname is correctly configured");
	}

	/**
	 * @param value
	 * @return  form validation of description
	 */
	public FormValidation doCheckDescription(@QueryParameter final String value) {
		if (!validator.isDescriptionPresent(value)) {
			return FormValidation.error("Server description is mandatory");
		}
		if (validator.isDescriptionTooLong(value)) {
			return FormValidation.error("Description is limited to 100 characters");
		}

		return FormValidation.ok("Description is correctly configured");
	}

	/**
	 * @param value
	 * @return  form validation of port.
	 */
	public FormValidation doCheckPort(@QueryParameter final String value) {
		if (!validator.isPortPresent(value)) {
			return FormValidation.error("Please set a port");
		}

		if (!validator.validatePortFormat(value)) {
			return FormValidation.error("Please check the port format");
		}

		return FormValidation.ok("Port is correctly configured");
	}

	/**
	 * 
	 * @param value
	 * @return  form validation of base queue name
	 */
	public FormValidation doCheckDatabaseName(@QueryParameter final String value) {
	    if(!validator.isBaseQueueNamePresent(value)){
	        return FormValidation.error("Database name is mandatory");
	    }

	    return FormValidation.ok("Database name is correctly Configured");
	    
	}
}
