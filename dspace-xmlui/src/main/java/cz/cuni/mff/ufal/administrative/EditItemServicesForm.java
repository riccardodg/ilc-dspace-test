/* Created for LINDAT/CLARIN */
package cz.cuni.mff.ufal.administrative;

import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.dspace.app.xmlui.aspect.administrative.FlowResult;
import org.dspace.app.xmlui.aspect.administrative.item.ViewItem;
import org.dspace.app.xmlui.cocoon.AbstractDSpaceTransformer;
import org.dspace.app.xmlui.wing.Message;
import org.dspace.app.xmlui.wing.WingException;
import org.dspace.app.xmlui.wing.element.Body;
import org.dspace.app.xmlui.wing.element.Cell;
import org.dspace.app.xmlui.wing.element.Division;
import org.dspace.app.xmlui.wing.element.List;
import org.dspace.app.xmlui.wing.element.PageMeta;
import org.dspace.app.xmlui.wing.element.Para;
import org.dspace.app.xmlui.wing.element.Radio;
import org.dspace.app.xmlui.wing.element.Row;
import org.dspace.app.xmlui.wing.element.Select;
import org.dspace.app.xmlui.wing.element.Text;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Bitstream;
import org.dspace.content.Bundle;
import org.dspace.content.Metadatum;
import org.dspace.content.Item;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;

import cz.cuni.mff.ufal.DSpaceApi;
import cz.cuni.mff.ufal.lindat.utilities.hibernate.LicenseDefinition;
import cz.cuni.mff.ufal.lindat.utilities.hibernate.LicenseLabel;
import cz.cuni.mff.ufal.lindat.utilities.interfaces.IFunctionalities;


/**
 * Display information about the item and allow the user to change 
 * 
 * @author Amir Kamran
 */


public class EditItemServicesForm extends AbstractDSpaceTransformer {
	
	Logger log = Logger.getLogger(EditItemServicesForm.class);

	/** Language strings */
	private static final Message T_dspace_home = message("xmlui.general.dspace_home");	
	private static final Message T_item_trail = message("xmlui.administrative.item.general.item_trail");
	private static final Message T_option_head = message("xmlui.administrative.item.general.option_head");
		
	private static final Message T_save = message("xmlui.general.save");
	private static final Message T_update = message("xmlui.general.update");
	private static final Message T_return = message("xmlui.general.return");
	
	private static final Message T_title = message("xmlui.administrative.item.EditItemServicesForm.title");
	private static final Message T_trail = message("xmlui.administrative.item.EditItemServicesForm.trail");
	
	public void addPageMeta(PageMeta pageMeta) throws WingException
	{
		pageMeta.addMetadata("title").addContent(T_title);
		
		pageMeta.addTrailLink(contextPath + "/", T_dspace_home);
		pageMeta.addTrailLink(contextPath + "/admin/item",T_item_trail);
		pageMeta.addTrail().addContent(T_trail);
	}

	public void addBody(Body body) throws SQLException, WingException
	{
		// Get our parameters and state
		int itemID = parameters.getParameterAsInteger("itemID",-1);
		
		String activate = parameters.getParameter("activate", null);
		String deactivate = parameters.getParameter("deactivate", null);
		
		Item item = Item.find(context, itemID);
		String baseURL = contextPath+"/admin/item?administrative-continue="+knot.getId();
		
		
		if(activate!=null) {
			item.clearMetadata("local", "featuredService", activate, item.ANY);
			item.addMetadata("local", "featuredService", activate, item.ANY, "true");
		}
		
		if(deactivate!=null) {
			item.clearMetadata("local", "featuredService", activate, item.ANY);
		}
		
		
		// DIVISION: main
		Division main = body.addInteractiveDivision("edit-item-services", contextPath+"/admin/item", Division.METHOD_POST,"primary administrative edit-item-services");
		main.setHead(T_option_head);

		String tabLink = baseURL + "&services";
		
		// LIST: options
		List options = main.addList("options", List.TYPE_SIMPLE, "horizontal");
		ViewItem.add_options(context, eperson, options, baseURL, ViewItem.T_option_services, tabLink);
		
		String featuredServices = ConfigurationManager.getProperty("lr", "featured.services");
		
		if(featuredServices!=null && !featuredServices.isEmpty()) {
			
			Division fsDiv = main.addDivision("featuredService", "");

			for(String featuredService : featuredServices.split(",")) {
								
				String name = ConfigurationManager.getProperty("lr", "featured.service." + featuredService + ".fullname");
				String url = ConfigurationManager.getProperty("lr", "featured.service." + featuredService + ".url");
				String description = ConfigurationManager.getProperty("lr", "featured.service." + featuredService + ".description");					

				Division fsInnerDiv = fsDiv.addDivision(featuredService, "well well-white").addDivision("caption", "caption");
				fsInnerDiv.addPara(null, "h3").addXref(url, name, "target_blank");
				fsInnerDiv.addPara(description);				
					
				Metadatum[] mds = item.getMetadataByMetadataString("local.featuredService." + featuredService);
				if(mds!=null && mds.length!=0 && mds[0].value.equals("true")) {
					fsInnerDiv.addPara().addXref(tabLink + "&deactivate=" + featuredService, "Deactivate", "btn btn-danger");
				} else {
					fsInnerDiv.addPara().addXref(tabLink + "&activate=" + featuredService, "Activate", "btn btn-info");
				}
			}
		
		} else {
			
		}

		main.addPara().addHidden("administrative-continue").setValue(knot.getId());

	}
	
}
