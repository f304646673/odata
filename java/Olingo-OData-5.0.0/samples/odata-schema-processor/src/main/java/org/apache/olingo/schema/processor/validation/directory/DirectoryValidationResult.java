package org.apache.olingo.schema.processor.validation.directory;

import org.apache.olingo.schema.processor.validation.file.XmlComplianceResult;

import java.util.*;

/**
 * Result of directory-level XML schema validation.
 * Contains comprehensive information about validation of multiple XML files in a directory,
 * including individual file results, cross-file conflicts, and overall compliance status.
 */
public class DirectoryValidationResult {
    
    private final boolean compliant;
    private final Map<String, XmlComplianceResult> fileResults;
    private final List<SchemaConflict> conflicts;
    private final List<String> globalErrors;
    private final List<String> globalWarnings;
    private final Map<String, Set<String>> namespaceToFiles;
    private final String directoryPath;
    private final long validationTimeMs;
    private final int totalFilesProcessed;
    private final int validFiles;
    private final int invalidFiles;
    
    /**
     * Constructor for DirectoryValidationResult
     * 
     * @param compliant whether all files in the directory are compliant and no conflicts exist
     * @param fileResults map of filename to individual validation results
     * @param conflicts list of detected schema conflicts across files
     * @param globalErrors global errors that affect the entire directory
     * @param globalWarnings global warnings for the directory
     * @param namespaceToFiles mapping of namespaces to files that define them
     * @param directoryPath path to the validated directory
     * @param validationTimeMs total validation time in milliseconds
     */
    public DirectoryValidationResult(boolean compliant, 
                                   Map<String, XmlComplianceResult> fileResults,
                                   List<SchemaConflict> conflicts,
                                   List<String> globalErrors,
                                   List<String> globalWarnings,
                                   Map<String, Set<String>> namespaceToFiles,
                                   String directoryPath,
                                   long validationTimeMs) {
        this.compliant = compliant;
        this.fileResults = Collections.unmodifiableMap(new HashMap<>(fileResults));
        this.conflicts = Collections.unmodifiableList(new ArrayList<>(conflicts));
        this.globalErrors = Collections.unmodifiableList(new ArrayList<>(globalErrors));
        this.globalWarnings = Collections.unmodifiableList(new ArrayList<>(globalWarnings));
        this.namespaceToFiles = Collections.unmodifiableMap(new HashMap<>(namespaceToFiles));
        this.directoryPath = directoryPath;
        this.validationTimeMs = validationTimeMs;
        
        // Calculate statistics
        this.totalFilesProcessed = fileResults.size();
        this.validFiles = (int) fileResults.values().stream().filter(XmlComplianceResult::isCompliant).count();
        this.invalidFiles = totalFilesProcessed - validFiles;
    }
    
    /**
     * @return true if all files are compliant and no cross-file conflicts exist
     */
    public boolean isCompliant() {
        return compliant;
    }
    
    /**
     * @return map of filename to individual validation results
     */
    public Map<String, XmlComplianceResult> getFileResults() {
        return fileResults;
    }
    
    /**
     * @return list of detected schema conflicts across files
     */
    public List<SchemaConflict> getConflicts() {
        return conflicts;
    }
    
    /**
     * @return global errors that affect the entire directory
     */
    public List<String> getGlobalErrors() {
        return globalErrors;
    }
    
    /**
     * @return global warnings for the directory
     */
    public List<String> getGlobalWarnings() {
        return globalWarnings;
    }
    
    /**
     * @return mapping of namespaces to files that define them
     */
    public Map<String, Set<String>> getNamespaceToFiles() {
        return namespaceToFiles;
    }
    
    /**
     * @return path to the validated directory
     */
    public String getDirectoryPath() {
        return directoryPath;
    }
    
    /**
     * @return total validation time in milliseconds
     */
    public long getValidationTimeMs() {
        return validationTimeMs;
    }
    
    /**
     * @return total number of files processed
     */
    public int getTotalFilesProcessed() {
        return totalFilesProcessed;
    }
    
    /**
     * @return number of valid files
     */
    public int getValidFiles() {
        return validFiles;
    }
    
    /**
     * @return number of invalid files
     */
    public int getInvalidFiles() {
        return invalidFiles;
    }
    
    /**
     * Get validation result for a specific file
     * 
     * @param fileName name of the file
     * @return validation result for the file, or null if not found
     */
    public XmlComplianceResult getFileResult(String fileName) {
        return fileResults.get(fileName);
    }
    
    /**
     * Check if a specific namespace has conflicts
     * 
     * @param namespace the namespace to check
     * @return true if the namespace has conflicts
     */
    public boolean hasNamespaceConflicts(String namespace) {
        return conflicts.stream().anyMatch(conflict -> namespace.equals(conflict.getNamespace()));
    }
    
    /**
     * Get all files that define a specific namespace
     * 
     * @param namespace the namespace
     * @return set of filenames that define the namespace
     */
    public Set<String> getFilesForNamespace(String namespace) {
        return namespaceToFiles.getOrDefault(namespace, Collections.emptySet());
    }
    
    /**
     * @return true if any conflicts were detected
     */
    public boolean hasConflicts() {
        return !conflicts.isEmpty();
    }
    
    /**
     * @return true if any global errors were detected
     */
    public boolean hasGlobalErrors() {
        return !globalErrors.isEmpty();
    }
    
    /**
     * Get a summary string of the validation results
     */
    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("Directory Validation Summary:\n");
        sb.append("  Directory: ").append(directoryPath).append("\n");
        sb.append("  Total Files: ").append(totalFilesProcessed).append("\n");
        sb.append("  Valid Files: ").append(validFiles).append("\n");
        sb.append("  Invalid Files: ").append(invalidFiles).append("\n");
        sb.append("  Conflicts: ").append(conflicts.size()).append("\n");
        sb.append("  Global Errors: ").append(globalErrors.size()).append("\n");
        sb.append("  Global Warnings: ").append(globalWarnings.size()).append("\n");
        sb.append("  Overall Compliant: ").append(compliant).append("\n");
        sb.append("  Validation Time: ").append(validationTimeMs).append("ms");
        return sb.toString();
    }
}
