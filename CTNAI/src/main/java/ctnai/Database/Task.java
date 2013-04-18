package ctnai.Database;

import java.io.File;
import java.io.IOException;

public class Task
{
    private Long id;
    private Long file;
    private Long owner;
    private Long result;
    
    private String type;
    private Boolean active;
    
    private Process process;
    
    private String progress;
    private String errorMessage;
    private String outputInformation;
    
    public Task()
    {
        progress = "0%";
        errorMessage = null;
        outputInformation = null;
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getFile()
    {
        return file;
    }

    public void setFile(Long file)
    {
        this.file = file;
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
        int outputLength = process.getInputStream().available();
        
        if (outputLength == 0)
        {
            return;
        }
        
        byte[] buffer = new byte [outputLength];
        
        process.getInputStream().read(buffer, 0, outputLength);
        
        String[] lines = new String(buffer).split("\\*");
        
        for (String line : lines)
        {
            if (!line.trim().startsWith("Round"))
            {
                if (outputInformation == null)
                {
                    outputInformation = line.trim();
                }
                else
                {
                    outputInformation += ("</BR>" + line.trim());
                }
            }
        }
        
        for (int i = 1; i < lines.length; i++)
        {
            String line;
            if ((line = lines[lines.length - i].trim()).startsWith("Round"))
            {
                String[] nums = line.substring(7, (line.length() - 1)).split("/");
                progress = (((100 * Integer.parseInt(nums[0])) / Integer.parseInt(nums[1])) + "%");
                break;
            }
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
    
    public void cancel()
    {
        if (!getFinished())
        {
            process.destroy();
        }
    }
    
    public static Task newTask(Long owner, Long file, Long result, String type)
    {
        Task task = new Task();
        
        task.setOwner(owner);
        task.setFile(file);
        task.setResult(result);
        task.setType(type);
        task.setActive(true);
        
        return task;
    }

    @Override
    public String toString()
    {
        return "Task ID: " + id + ", on file ID: " + file + ", executed by user ID: " + owner + '.';
    }
}
