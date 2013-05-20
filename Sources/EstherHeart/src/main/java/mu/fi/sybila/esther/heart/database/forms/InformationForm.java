package mu.fi.sybila.esther.heart.database.forms;

import javax.servlet.http.HttpServletRequest;
import mu.fi.sybila.esther.heart.database.entities.UserInformation;

/**
 * Data Model for user information.
 * 
 * @author George Kolcak
 */
public class InformationForm
{
    
    private String country;
    private String organization;
    
    private Boolean hidePublicOwned;
    
    /**
     * Static method for obtaining the user information data from a HTTP request.
     * 
     * @param request The HTTP request with the user information.
     * @return The created user information form.
     */
    public static InformationForm extractFromRequest(HttpServletRequest request)
    {
        InformationForm form = new InformationForm();
        
        String country = request.getParameter("country"); 
        String organization = request.getParameter("organization");
        
        form.setCountry((country.equals("NULL") ? null : country));
        form.setOrganization((organization.isEmpty() ? null : organization));
        
        return form;
    }
    
    /**
     * Static method for converting user information into information form.
     * 
     * @param information The user information with the coveted data.
     * @return The created information form.
     */
    public static InformationForm extractFromUserInformation(UserInformation information)
    {
        InformationForm form = new InformationForm();
        
        form.setCountry(information.getCountry());
        form.setOrganization(information.getOrganization());
        form.setHidePublicOwned(information.getHidePublicOwned());
        
        return form;
    }
    
    /**
     * Validates user information data.
     * 
     * @param errorOutput StringBuilder used as output in case of error in the data.
     * @return A new user information with the validated data.
     */
    public UserInformation validate(StringBuilder errorOutput)
    {
        UserInformation info = new UserInformation();
        
        info.setHidePublicOwned(true);
        info.setCountry(country);
        
        if (validateOrganization(errorOutput))
        {
            info.setOrganization(organization);
        }
        
        return info;
    }
    
    /**
     * Validates the value of the organization attribute.
     * 
     * @param errorOutput StringBuilder used as output in case of error in the data.
     * @return True if the organization value is valid. False otherwise.
     */
    public boolean validateOrganization(StringBuilder errorOutput)
    {
        if ((organization != null) && (organization.length() > 128))
        {
            errorOutput.append("Organization name cannot be longer than 128 characters.");
            errorOutput.append("<br/>\n");
            
            return false;
        }
        
        return true;
    }

    public String getCountry()
    {
        return country;
    }

    public void setCountry(String country)
    {
        this.country = country;
    }

    public String getOrganization()
    {
        return organization;
    }

    public void setOrganization(String organization)
    {
        this.organization = organization;
    }

    public Boolean getHidePublicOwned()
    {
        return hidePublicOwned;
    }

    public void setHidePublicOwned(Boolean hidePublicOwned)
    {
        this.hidePublicOwned = hidePublicOwned;
    }
    
}
