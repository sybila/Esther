package mu.fi.sybila.esther.heart.database.entities;

import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Database entity representing a long-term task.
 * 
 * @author George Kolcak
 */
public class Task
{
    public static final String[] PARSYBONE_OPERATIONS =
    {
        "Testing edge constraints on partiall parametrizations",
        "Creating transitions for state",
        "Building automaton state",
        "Building product state",
        "Round"
    };
    
    public static final String[] BEHAVIOUR_MAPPER_OPEARTIONS =
    {
        "Reading parameters from databases",
        "Mapping input parametrizations",
        "Computing node sizes",
        "Computing edge thickness",
        "Layouting nodes",
        "Exporting nodes",
        "Exporting edges"
    };
    
    private Long id;
    private Long model;
    private Long property;
    private List<Long> databases;
    private List<Long> filters;
    
    private Long owner;
    private Long result;
    
    private String type;
    
    private Boolean finished;
    private Boolean active;
    
    private String command;
    private Date date;
    
    //private Process process;
    
    private String progress;
    private String errorMessage;
    private String outputInformation;
    
    //private String outputResidue;
    
    private String modelName;
    private String propertyName;
    private List<String> databaseNames;
    private List<String> filterNames;
    
    /**
     * Default Task constructor.
     */
    public Task()
    {
        progress = "0%";
        errorMessage = null;
        outputInformation = null;
        
        model = null;
        property = null;
        databases = new ArrayList<>();
        filters = new ArrayList<>();
        
        modelName = "unknown model";
        propertyName = "unknown property";
        databaseNames = new ArrayList<>();
        filterNames = new ArrayList<>();
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

    public List<Long> getDatabases()
    {
        return Collections.unmodifiableList(databases);
    }

    public void addDatabase(Long database)
    {
        databases.add(database);
    }
    
    public boolean removeDatabase(Long database)
    {
        return databases.remove(database);
    }

    public List<Long> getFilters()
    {
        return Collections.unmodifiableList(filters);
    }

    public void addFilter(Long filter)
    {
        filters.add(filter);
    }
    
    public boolean removeFilter(Long filter)
    {
        return filters.remove(filter);
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
    
//    public void setProcess(Process process)
//    {
//        this.process = process;
//    }
    
    public boolean getFinished()
    {
        return finished;
//        try
//        {
//            process.exitValue();
//            
//            return true;
//        }
//        catch (IllegalThreadStateException e)
//        {
//            return false;
//        }
    }
    
    public void setFinished(Boolean finished)
    {
        this.finished = finished;
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
//        if (errorMessage != null)
//        {
//            return errorMessage;
//        }
        
        if (!getFinished())
        {
            return null;
        }
        
//        int errorLength;
//        if ((errorLength = process.getErrorStream().available()) <= 0)
//        {
//            return null;
//        }
//        
//        byte[] buffer = new byte[errorLength];
//        process.getErrorStream().read(buffer, 0, errorLength);
//        
//        errorMessage = new String(buffer).substring(1).trim();
        
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
            if (!getSuccessful())
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
        
//        readOutput();
        
        return progress;
    }
    
//    private void readOutput() throws IOException
//    {   
//        byte[] buffer;
//        
//        synchronized(process)
//        {
//            int outputLength = process.getInputStream().available();
//        
//            if (outputLength == 0)
//            {
//                return;
//            }
//
//            buffer = new byte [outputLength];
//
//            process.getInputStream().read(buffer, 0, outputLength);
//        }
//                
//        String output = "";
//        
//        if (outputResidue != null)
//        {
//            output = outputResidue;
//            outputResidue = null;
//        }
//        
//        output = output.concat(new String(buffer));
//        
//        String[] lines = output.split("[\n\r]");
//        
//        String lastProgressLine = null;
//        
//        for (String line : lines)
//        {
//            if (line.trim().isEmpty())
//            {
//                continue;
//            }
//            
//            if (!line.trim().endsWith("."))
//            {
//                outputResidue = line;
//                break;
//            }
//            
//            String trimmedLine = line.trim().substring(2).trim();
//            
//            if (line.trim().startsWith("*"))
//            {
//                if (outputInformation == null)
//                {
//                    outputInformation = trimmedLine;
//                }
//                else
//                {
//                    outputInformation += ("</BR>" + trimmedLine);
//                }
//            }
//            else if (line.trim().startsWith("#"))
//            {
//                lastProgressLine = trimmedLine;
//            }
//        }
//        
//        if (lastProgressLine != null)
//        {
//            String[] parts = lastProgressLine.split(":");
//                
//            String operation = parts[0].trim();
//
//            StringBuilder progressBuilder = new StringBuilder();
//
//            for (int i = 0; i < 5; i++)
//            {
//                if (PARSYBONE_OPERATIONS[i].equals(operation))
//                {
//                    progressBuilder.append("[");
//                    progressBuilder.append(i + 1);
//                    progressBuilder.append("/5] ");
//
//                    progressBuilder.append(operation);
//                    progressBuilder.append(": ");
//
//                    break;
//                }
//            }
//            
//            String[] nums = parts[1].trim().split("/");
//                    
//            int round = Integer.parseInt(nums[0]);
//            int total = Integer.parseInt(nums[1].substring(0, (nums[1].length() - 1)));
//
//            progressBuilder.append((100 * round) / total);
//            progressBuilder.append("%");
//                
//            progress = progressBuilder.toString();
//        }
//    }
    public String getCommand()
    {
        return command;
    }

    public void setCommand(String command)
    {
        this.command = command;
    }

    public Date getDate()
    {
        return date;
    }

    public void setDate(Date date)
    {
        this.date = date;
    }
    
    public void setInformation(String information)
    {
        outputInformation = information;
    }
    
    public String getInformation() throws IOException
    {   
//        String error = getError();
//        if (error != null)
//        {
//            return error;
//        }
        
//        readOutput();
        
        if (errorMessage != null)
        {
            return errorMessage;
        }
        
        return outputInformation;
    }
    
//    public String getOutputResidue()
//    {
//        return outputResidue;
//    }
//    
//    public void setOutputResidue(String value)
//    {
//        outputResidue = value;
//    }
    
//    public void cancel()
//    {
//        if (!getFinished())
//        {
//            process.destroy();
//        }
//    }

    public String getModelName()
    {
        return modelName;
    }

    public void setModelName(String modelName)
    {
        this.modelName = modelName;
    }

    public String getPropertyName()
    {
        return propertyName;
    }

    public void setPropertyName(String propertyName)
    {
        this.propertyName = propertyName;
    }

    public List<String> getDatabaseNames()
    {
        return databaseNames;
    }

    public void setDatabaseNames(List<String> databaseNames)
    {
        this.databaseNames = databaseNames;
    }

    public List<String> getFilterNames()
    {
        return filterNames;
    }

    public void setFilterNames(List<String> filterNames)
    {
        this.filterNames = filterNames;
    }
    
    public String getText()
    {
        StringBuilder textBuilder = new StringBuilder();
        
        switch (getType())
        {
            case "behaviour_mapper":
            {
                textBuilder.append("Behaviour Mapper on ");
                
                for (int i = 0; i < databases.size(); i++)
                {
                    if (i > 0)
                    {
                        if (i == (databases.size() - 1))
                        {
                            textBuilder.append(", ");
                        }
                        else   
                        {
                            textBuilder.append(" and ");
                        }
                    }
                    
                    textBuilder.append(getDatabaseNames().get(i));
                }
                
                textBuilder.append(" (Model: ");
                textBuilder.append(getModelName());
                textBuilder.append(", Property: ");
                textBuilder.append(getPropertyName());
                textBuilder.append(")");
                
                if (filters.size() > 0)
                {
                    textBuilder.append(" filtered by ");

                    for (int i = 0; i < filters.size(); i++)
                    {
                        if (i > 0)
                        {
                            textBuilder.append(", ");
                        }

                        textBuilder.append(getFilterNames().get(i));
                    }
                }
                
                break;
            }
            case "parsybone":
            {
                textBuilder.append("Parsybone on ");
                textBuilder.append(getModelName());
                textBuilder.append(" with ");
                textBuilder.append(getPropertyName());
                
                if (databases.size() > 0)
                {
                    textBuilder.append(" filtered by ");

                    for (int i = 0; i < databases.size(); i++)
                    {
                        if (i > 0)
                        {
                            textBuilder.append(", ");
                        }

                        textBuilder.append(getDatabaseNames().get(i));
                    }
                }
                
                break;
            }
            default: return null;
        }
        
        return textBuilder.toString();
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
        task.setFinished(false);
        
        return task;
    }

    @Override
    public String toString()
    {
        return "Task ID: " + id + ", on Model ID: " + model + ", with Property ID: " + property +
                ", executed by user ID: " + owner + '.';
    }
}
