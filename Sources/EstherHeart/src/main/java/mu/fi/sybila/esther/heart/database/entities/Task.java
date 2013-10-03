package mu.fi.sybila.esther.heart.database.entities;

import java.io.IOException;

/**
 * Database entity representing a long-term task.
 * 
 * @author George Kolcak
 */
public class Task
{
    private static final String[] PARSYBONE_OPERATIONS =
    {
        "Testing edge constraints on partiall parametrizations",
        "Creating transitions for state",
        "Building automaton state",
        "Building product state",
        "Round"
    };
    
    private Long id;
    private Long model;
    private Long property;
    private Long owner;
    private Long result;
    
    private String type;
    private Boolean active;
    
    private Process process;
    
    private String progress;
    private String errorMessage;
    private String outputInformation;
    
    private String outputResidue;
    
    /**
     * Default Task constructor.
     */
    public Task()
    {
        progress = "0%";
        errorMessage = null;
        outputInformation = null;
        
        outputResidue = null;
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getModel()
    {
        return model;
    }

    public void setModel(Long file)
    {
        this.model = file;
    }

    public Long getProperty()
    {
        return property;
    }

    public void setProperty(Long file)
    {
        this.property = file;
    }

    public Long getOwner()
    {
        return owner;
    }

    public void setOwner(Long owner)
    {
        this.owner = owner;
    }

    public Long getResult()
    {
        return result;
    }

    public void setResult(Long result)
    {
        this.result = result;
    }

    public Boolean getActive()
    {
        return active;
    }

    public void setActive(Boolean active)
    {
        this.active = active;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }
    
    public void setProcess(Process process)
    {
        this.process = process;
    }
    
    public boolean getFinished()
    {
        try
        {
            process.exitValue();
            
            return true;
        }
        catch (IllegalThreadStateException e)
        {
            return false;
        }
    }
    
    public boolean getSuccessful()
    {
        return (errorMessage == null);
    }
    
    public void setError(String error)
    {
        errorMessage = error;
    }
    
    public String getError() throws IOException
    {
        if (errorMessage != null)
        {
            return errorMessage;
        }
        
        if (!getFinished())
        {
            return null;
        }
        
        int errorLength;
        if ((errorLength = process.getErrorStream().available()) <= 0)
        {
            return null;
        }
        
        byte[] buffer = new byte[errorLength];
        process.getErrorStream().read(buffer, 0, errorLength);
        
        errorMessage = new String(buffer).substring(1).trim();
        
        return errorMessage;
    }
    
    public void setProgress(String progress)
    {
        this.progress = progress;
    }
    
    public String getProgress() throws IOException
    {
        if (getFinished())
        {
            if (getError() != null)
            {
                return "Error";
            }
            
            if (getActive())
            {
                return "Ready";
            }
            else
            {
                return "Finished";
            }
        }
        
        readOutput();
        
        return progress;
    }
    
    private void readOutput() throws IOException
    {   
        byte[] buffer;
        
        synchronized(process)
        {
            int outputLength = process.getInputStream().available();
        
            if (outputLength == 0)
            {
                return;
            }

            buffer = new byte [outputLength];

            process.getInputStream().read(buffer, 0, outputLength);
        }
                
        String output = "";
        
        if (outputResidue != null)
        {
            output = outputResidue;
            outputResidue = null;
        }
        
        output = output.concat(new String(buffer));
        
        String[] lines = output.split("[\n\r]");
        
        String lastProgressLine = null;
        
        for (String line : lines)
        {
            if (line.trim().isEmpty())
            {
                continue;
            }
            
            if (!line.trim().endsWith("."))
            {
                outputResidue = line;
                break;
            }
            
            String trimmedLine = line.trim().substring(2).trim();
            
            if (line.trim().startsWith("*"))
            {
                if (outputInformation == null)
                {
                    outputInformation = trimmedLine;
                }
                else
                {
                    outputInformation += ("</BR>" + trimmedLine);
                }
            }
            else if (line.trim().startsWith("#"))
            {
                lastProgressLine = trimmedLine;
            }
        }
        
        if (lastProgressLine != null)
        {
            String[] parts = lastProgressLine.split(":");
                
            String operation = parts[0].trim();

            StringBuilder progressBuilder = new StringBuilder();

            for (int i = 0; i < 5; i++)
            {
                if (PARSYBONE_OPERATIONS[i].equals(operation))
                {
                    progressBuilder.append("[");
                    progressBuilder.append(i + 1);
                    progressBuilder.append("/5] ");

                    progressBuilder.append(operation);
                    progressBuilder.append(": ");

                    break;
                }
            }
            
            String[] nums = parts[1].trim().split("/");
                    
            int round = Integer.parseInt(nums[0]);
            int total = Integer.parseInt(nums[1].substring(0, (nums[1].length() - 1)));

            progressBuilder.append((100 * round) / total);
            progressBuilder.append("%");
                
            progress = progressBuilder.toString();
        }
    }
    
    public void setInformation(String information)
    {
        outputInformation = information;
    }
    
    public String getInformation() throws IOException
    {   
        String error = getError();
        if (error != null)
        {
            return error;
        }
        
        readOutput();
        
        return outputInformation;
    }
    
    public String getOutputResidue()
    {
        return outputResidue;
    }
    
    public void setOutputResidue(String value)
    {
        outputResidue = value;
    }
    
    public void cancel()
    {
        if (!getFinished())
        {
            process.destroy();
        }
    }
    
    public static Task newTask(Long owner, Long model, Long property, Long result, String type)
    {
        Task task = new Task();
        
        task.setOwner(owner);
        task.setModel(model);
        task.setProperty(property);
        task.setResult(result);
        task.setType(type);
        task.setActive(true);
        
        return task;
    }

    @Override
    public String toString()
    {
        return "Task ID: " + id + ", on Model ID: " + model + ", with Property ID: " + property +
                ", executed by user ID: " + owner + '.';
    }
}
