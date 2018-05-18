# What is Fit3D?
Fit3D is a software tool for the template-based and template-free detection of structural motifs in macromolecular structure data. This data is usually served from the [Protein Data Bank](http://www.rcsb.org/).

# When should I use Fit3D?
You can use Fit3D to detect small conserved structural motifs in protein structures (or other types of macromolecular structures, e.g. DNA/RNA). This can be useful to annotate the function 
proteins, to identify similar binding sites for drug target prediction, or simply to study molecular mechanisms at atomic level. If you want to learn more about the importance of structural motifs 
please refer to [Kaiser _et al._ 2016](https://doi.org/10.1093/bioinformatics/btv637) or [Kaiser _et al._ 2018](https://doi.org/10.1371/journal.pcbi.1006101).
In contrast to most competitors, Fit3D makes use of full atomic resolution data found in the PDB. Key features of Fit3D are:

- a user-defined selection of atoms to represent structural motifs computationally, and
- the definition of so-called position-specific exchanges to cover isofunctional substitutions of residues,
- the availability as command line implementation, [web server](https://biosciences.hs-mittweida.de/fit3d/home), and as flexible [API](https://github.com/cleberecht/singa/wiki/Structure-Alignments-(Chemistry)).

### With Fit3D you can

1. use a defined template structural motif to search for similar occurrences in arbitrary-sized datasets of structures
2. provide an arbitrary set of protein structures (or a single protein chain) to detect conserved structural motifs in a template-free manner

The first scenario can be useful, for example, to annotate the function of proteins by searching for templatestructural motifs of known function (e.g. derived from the 
[Catalytic Site Atlas](https://www.ebi.ac.uk/thornton-srv/databases/CSA/)). Or, if you have identified a particular motif of interest, you can use it as template for a PDB-wide screening of similar
 motifs. The template-free application should be used to investigate whether geometrically conserved structural motifs are present in a provided dataset of protein structures. This can be very 
 useful to identify motifs of potential functional or structural relevance without any _a priori_ knowledge. Fit3D focuses on the template-free detection of long-range contacts that are hardly 
 detectable by sequence analysis.

### How it works
__Template-based detection__ with Fit3D is a combinatorial approach based on the generation of match candidates in local environments. For details on how the algorithm works please refer to 
[Kaiser _et al._ 2015](https://doi.org/10.1089/cmb.2014.0263).

__Template-free detection__ exploits a popular data mining technology, so-called itemset mining. For Fit3D, itemset mining was adapted and extended to deal with macromolecular structure 
data or spatial data in general. For details on how this works please refer to [Kaiser _et al._ 2017](https://doi.org/10.1109/tcbb.2017.2786250).

# Getting started
In order to run the command line version of Fit3D nothing but an installation of [Java Runtime Environment](https://java.com/de/download/) 1.8 or later is required. Optionally, [R](https://www.r-project.org/)
version 3.4.x or later is necessary to calculate _p_-values of reported matches when using the statistical model of [Fofanov _et al._](https://ieeexplore.ieee.org/abstract/document/4686202/)

## Command line version of Fit3D
Download the latest release of Fit3D from [GitHub](https://github.com/fkaiserbio/fit3d/releases). The software is shipped as a runnable `jar` file. To run it on your machine type:

`java -jar Fit3D.jar`

This will present you the help dialog of Fit3D. Two basic command are available to run Fit3D either in template-based or template-free mode:

1. template-based: `java -jar Fit3D.jar template-based -m <arg> [-t <arg> | -l <arg>] [OPTIONS]`
2. template-free: `java -jar Fit3D.jar template-free [-t <arg> | -l <arg> | -d <arg>] -o <arg> [OPTIONS]`

__Note:__ for the processing of many structures it is advisable to increase the heap space of the Java virtual machine by appending the `-Xmx` option: e.g. `java -Xmx6G Fit3D.jar` to allocate 6GB 
of RAM
 
### Quickstart: Template-based
In order to run a template-based structural motif detection with Fit3D you need specify: a template motif in PDB format or use the integrated extraction wizard, the PDB-ID of a target protein or a 
list of PDB-IDs for multiple targets.

This simple command searches for the catalytic triad of serine proteases, extracted from the structure PDB:1gl0 provided in PDB format as file `1gl0.pdb`, in a set of PDB structures provided as 
list of 
PDB-IDs in the file `targets.txt`:

`java -jar Fit3D.jar template-based -X E-H57_E-D102_E-S195 -m 1gl0.pdb -l targets.txt`

#### Output
When run in template-based mode, Fit3D will output matches similar to the template motif. If no additional options (see Advanced Options) are specified, the results will be written to the standard 
output in comma-separated values (CSV) format.


### Quickstart: Template-free
To run a template-free structural motif detection with Fit3D you need to specify: a target PDB chain which is used to get similar structures from the PDB REST API, a list of PDB chains or a local 
directory that contains structures in PDB format and an output directory where results will be written. The following command detects geometrically similar structural motifs in cupredoxin structures 
and writes the output to the directory `results/`.

`java -jar Fit3D.jar template-free -t 1gy2.A -o results/`

#### Output
When run in template-free mode, Fit3D will output the found structural motifs, clustered by structural similarity, in PDB format to the specified output directory. Additionally, the coverage of 
geometrically conserved structural motifs is written to a reference structure, encoded in B-factors. This structure can be conveniently visualized in [PyMOL](https://pymol.org/2/) by loading the provided `pml` script file. 

### Advanced Options

The Fit3D command line software offers a variety of advanced options to customize the structural motif detection. The following table provides an overview of these options.

| mode           | short | long                   | description                                                                                                                                                                                                                                                                                                                                  | default                                   |
|----------------|-------|------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-------------------------------------------|
| both           | -a    | --atoms                | The identifiers of atoms that should be used to represent amino acids according to the PDB nomenclature. Identifiers must be separated by comma, e.g. 'N,CA,C,O' to use all backbone atoms. Conflicts with '-R'.                                                                                                                             | no hydrogen                               |
| both           | -F    | --mmtf                 | Enables fast MMTF parsing of structures. If you specified a local PDB installation, it must contain MMTF structures. This is not compatible with the '-i' flag in template-free mode                                                                                                                                                         | false                                     |
| both           | -h    | --help                 | Displays the help dialog.                                                                                                                                                                                                                                                                                                                    |                                           |
| both           | -p    | --pdb                  | Path to a local PDB installation. Structures in this folder must be stored according to the PDB standard hierarchy: data/structures/divided/pdb/ac/pdb1acj.ent.gz for PDB format and data/structures/divided/mmtf/ac/1acj.mmtf.gz                                                                                                            | none                                      |
| both           | -R    | --scheme               | The representation scheme to represent amino acids. Must be one of: CA (alpha carbon), CB (beta carbon), CO (centroid), LH (last heavy side chain atom), SC (centroid of side chain atoms). Conflicts with '-a'.                                                                                                                             | none                                      |
| template-based | -d    | --distance-tolerance   | Allowed tolerance in Å for the extraction of local environments based on the spatial extent of the template motif.                                                                                                                                                                                                                           | 1.00 Å                                    |
| template-based | -e    | --exchange-residues    | The definition of position-specific exchanges allowed for matching against the template motif. The syntax is [motif residue number]:[allowed residues one-letter code],... For example, '12:AW,43:P' allows the template motif residue 12 to be matches against alanine and tryptophan. Residue 43 is allowed to be matched against proline. | none                                      |
| template-based | -f    | --result-file          | Specifies the path to the result file that will be written in CSV format.                                                                                                                                                                                                                                                                    | none                                      |
| template-based | -l    | --target-list          | A simple text file that contains target structures separated by line break. This file may either contain entries in the format [PDB-ID], [PDB-ID].[chain ID], or paths to structures in PDB format.                                                                                                                                          | none                                      |
| template-based | -m    | --motif                | Path to the template motif in PDB format.                                                                                                                                                                                                                                                                                                    | none                                      |
| template-based | -n    | --num-threads          | Number of threads used for the calculation.                                                                                                                                                                                                                                                                                                  | maximum                                   |
| template-based | -M    | --pfam-mapping         | Enables the mapping of Pfam identifiers of matches via the SIFTS project. Requires Internet access.                                                                                                                                                                                                                                          | true                                      |
| template-based | -P    | --p-values             | Enables the calculation of _p_-values for matches according to Fofanov et al. or Stark et al. Argument must either 'F' or 'S'.                                                                                                                                                                                                              | none                                      |
| template-based | -r    | --rmsd                 | The upper bound of the RMSD up to which matches should be reported.                                                                                                                                                                                                                                                                          | 2.00 Å                                    |
| template-based | -t    | --target               | A single target structure used for detection of the template motif. Can be either [PDB-ID], [PDB-ID].[chain ID], or a path to a structure in PDB format.                                                                                                                                                                                     | none                                      |
| template-based | -U    | --uniprot-mapping      | Enables the mapping of UniProt identifiers of matches via the SIFTS project. Requires Internet access.                                                                                                                                                                                                                                       | true                                      |
| template-based | -X    | --extract              | Extracts the motif from the input structure specified with the '-m' option. Follows the syntax: [chain]-[residue type][residue number]_... (e.g. E-H57_E-D102_E-S195)                                                                                                                                                                        | none                                      |
| template-free  | -c    | --config               | Path to a user-defined config file in JSON format for the template-free detection algorithm. This is only recommended for expert users.                                                                                                                                                                                                      | none                                      |
| template-free  | -d    | --target-structures    | Path to a directory that contains the target structures that should be used for detection.                                                                                                                                                                                                                                                   | none                                      |
| template-free  | -i    | --interactions         | Enables the annotation of noncovalent inter-residue interactions with PLIP. Requires Internet access and conflicts with '-F'.                                                                                                                                                                                                                | false                                     |
| template-free  | -l    | --target-chain-list    | A simple text file that contains target structures separated by line break. This file must contain entries in the format [PDB-ID].[chain ID].                                                                                                                                                                                                | none                                      |
| template-free  | -m    | --mapping              | Use a mapping scheme to group residues. Must be either 'C' (chemical groups) or 'F' (functional groups).                                                                                                                                                                                                                                     | none                                      |
| template-free  | -n    | --reference-chain      | The reference chain that is used to visualize the coverage of geometrically conserved structural motifs. Must follow the format [PDB-ID].[chain ID].                                                                                                                                                                                         | the first structure in the target dataset |
| template-free  | -o    | --output-directory     | Path to a directory where all results will be written.                                                                                                                                                                                                                                                                                       | none                                      |
| template-free  | -r    | --representative-level | Level of % sequence similarity used for the automatic retrieval of representative structures via PDB REST services. This is only used if a single target chain is specified with '-t'. Must be one of: 100, 95, 90, 70, 50, 40, or 30. Requires Internet access.                                                                             | 70%                                       |
| template-free  | -t    | --target-chain         | The target chain used for template-free structural motif detection. Similar structures are automatically retrieved from via PDB REST services. Requires Internet access.                                                                                                                                                                     | none                                      |

## Web server version of Fit3D
Currently, there is only a web server version of template-based detection with Fit3D available. It can be found at 
[https://biosciences.hs-mittweida.de/fit3d/home](https://biosciences.hs-mittweida.de/fit3d/home). The web server version covers the whole process starting from the definition of the template motif,
 up to the actual detection and the assessment of matches. The results of each run are interactively visualized in the browser.
 
## API version of Fit3D
For advanced users the [API](https://github.com/cleberecht/singa/wiki/Structure-Alignments-(Chemistry)) version of Fit3D is the method of choice. Template-based detection is directly integrated in the
[SiNGA](https://github.com/cleberecht/singa) framework. However, as template-free detection requires complicated algorithms that are not part of SiNGA, it is available as own project at 
[https://github.com/fkaiserbio/mmm](https://github.com/fkaiserbio/mmm). Please note that the template-free detection API is in an early state of development.