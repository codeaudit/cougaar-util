#!/usr/bin/perl -w

#  Copyright 2000 Defense Advanced Research Projects
#  Agency (DARPA) and ALPINE (a BBNT Solutions LLC (BBN)  Consortium).
#  This software to be used only in accordance with the
#  COUGAAR licence agreement.
#

use strict;

my($scalesize);
my($i);
my($templateDirectory);
my($targetDirectory);

$scalesize = 1;
$templateDirectory = ".";
$targetDirectory = ".";

for ($i = 0; $i < @ARGV; $i++) {
    if ($ARGV[$i] eq '-size') {
	if (defined ($ARGV[++$i])) {
	    $scalesize =  $ARGV[$i];
	}
    } elsif ($ARGV[$i] eq '-cleanup') {
	cleanup();
	exit;
    } elsif ($ARGV[$i] eq '-sourcedir') {
	if (defined ($ARGV[++$i])) {
	    $templateDirectory = $ARGV[$i];
	} else {
	    usage();
	}
    } elsif ($ARGV[$i] eq '-targetdir') {
	if (defined ($ARGV[++$i])) {
	    $targetDirectory = $ARGV[$i];
	} else {
	    usage();
 }
    } else { 
	usage(); 
    }
}
scaleUp($scalesize);



sub usage {
    print"usage\tscale [-size numberOfBrigadeNodes ]\n";
    print"\t\t[-sourcedir templateDirectory]\n";
    print"\t\t[-targetdir outputDiretory]\n";
    print"\t\t| [-cleanup]\n\n";
    print"\tThis script creates a set of Node files to be used together as an\n";
    print"\tCOUGAAR Society. The names of the new files are printed to STDOUT.\n";
    print"\n\tThe script creates a set of brigade Node files containing a set of\n";
    print"\tbattalions and an FSB plus a Node file containing a set of Division \n";
    print"\tlevel organizations. The number of brigade Node files created\n";
    print"\tis determined by the -size parameter.\n";
    print"\n\tThe new prototype-ini.dat and .ini files are created using existing\n";
    print"\tfiles as templates. Those template files are expected to be in current\n";
    print"\tdirectory unless -sourcedir is specified.\n";
    print"\n\t\t-size NUM\n\t\t\tThe number of brigade/FSB Nodes to create - default is 1\n";
    print"\n\t\t-sourcedir directory \n\t\t\tdirectory containing alpine integ-config files  \n\t\t\t - default is current directory\n";
    print"\n\t\t-targetdir directory \n\t\t\tdirectory where new society files are to be placed \n\t\t\t - default is current directory\n";
    print"\n\t\t-cleanup\n\t\t\tDelete all files created by this script\n";
    print"\n\t\t-help\n\t\t\tPrint this message\n";
    exit;
}

sub scaleUp{
    my($brigadeNodes) = @_;

    my(@allNewOrgs);
    my(@supportFsbOrgs);
    my(@newFSBs);
    my($i);
    my($org);
    my(@noChangeOrgs);
    my($oplanFilename);

    $oplanFilename = "oplanScale.xml";

    # Just use the regular .ini and -prototype-ini.dat files for these orgs when running.
    # Don't make copies or alter them here. Just include them in the new 3IDScaleNode.ini file
    @noChangeOrgs = ( 'TRANSCOM', 'HNS', 'DISCOM-3ID', 
		     'DIVARTY-3ID', 'ENGBDE-3ID');
	    
    #clone these orgs and make them support the new FSBs that will be created
    @supportFsbOrgs = ("IOC",  "FUTURE",  "DFSP",  "703-MSB");


    # open the new oplan file and write the initial parts
    startOplan($oplanFilename);

    for ($i = 1; $i <= $brigadeNodes; $i++) {
	my($newFSB);
	my($newBDE);
	my($neworg);
	my($templateorg);
	my(%likeOrg);
	my(@newBNs);

	@newBNs = ("$i-99-ARBN", "$i-88-INFBN", "$i-99-INFBN", "$i-99-ENGBN", "$i-99-FABN");
	$newFSB = "$i-ScaleFSB";
	$newBDE = "$i" . "ScaleBDE-3ID";

	#use the values as templates for the keys
	%likeOrg = ( $newFSB, '3-FSB',
		     $newBDE, '1BDE-3ID',
		     $newBNs[0], '3-69-ARBN',
		     $newBNs[1], '2-7-INFBN',
		     $newBNs[2], '3-7-INFBN',
		     $newBNs[3], '10-ENGBN',
		     $newBNs[4], '1-41-FABN');

	# keep track of all the new FSB orgs so other orgs can support them
	push(@newFSBs, $newFSB);

	# keep track of all the new orgs so 3ID can support them
	@allNewOrgs = (@allNewOrgs, keys(%likeOrg));

	while (($neworg, $templateorg) = each %likeOrg) {
	    #create new .ini file using the template
	    newDotIni($templateorg, $neworg);
	    #create new .inv files 
	    newInv($templateorg, $neworg);
	    #add this org to the oplan
	    addOplanOrg($neworg);
	    if ($templateorg eq '3-FSB') {
		#create new x-ScaleFSB-prototype-ini.dat file that supports the new battalions
		newSupport($templateorg, $neworg, '',  @newBNs);
	    } elsif ($templateorg eq '1BDE-3ID') {
		#create new xScaleBDE-3ID-prototype-ini.dat file
		newSupport($templateorg, $neworg, '3IDScale');
	    } else {
		#create new battalion -prototype-ini.dat file with the new brigade as its superior
		newBNProtoIni($templateorg, $neworg, $newBDE);
	    }
	}

	# create new xScaleBDE-3IDNode.ini file containing the new BNs, FSB, and brigade
	newNode("$newBDE", keys(%likeOrg));
    }

    #print "@allNewOrgs\n";


    my(@newSupportFsbOrgs);
    foreach $org (@supportFsbOrgs) {
	my($neworg);
	$neworg = "$org" . "Scale";
	push (@newSupportFsbOrgs, $neworg);
	# create -prototype-ini.dat that supports the new FSB orgs
	newSupport($org, $neworg, '',  @newFSBs);
	# create new .ini file for neworg based on org
	newDotIni($org, $neworg);
	# create new .inv files for ants
	newInv($org, $neworg);
    }

    #add MSB to oplan
    addOplanOrg('703-MSBScale');

    finishOplan();

    # create new 3IDScale-prototype-ini.dat file that supports all the new organizations
    newSupport('3ID', '3IDScale', 'XVIIICorpsScale',  @allNewOrgs);
    # create new 3IDScaleNode.ini
    newNode('3IDScale', ('3IDScale', 'CENTCOMScale', 'XVIIICorpsScale', @noChangeOrgs,  @newSupportFsbOrgs));
    # create new 3IDScale.ini using 3ID.ini as its template.
    newDotIni('3ID', '3IDScale');
    # create new CENTCOMScale.ini that uses new oplan file
    newDotIni('CENTCOM', 'CENTCOMScale', $oplanFilename);
    # create new -prototype-ini.dat, no support changes
    newSupport('CENTCOM', 'CENTCOMScale');
    #create new XVIIICorpsScale
    newDotIni('XVIIICorps', 'XVIIICorpsScale', $oplanFilename);
    # create new -prototype-ini.dat, no support changes
    newBNProtoIni('XVIIICorps', 'XVIIICorpsScale', 'CENTCOMScale');
}

sub addSupport{
#replace superior, add new supported orgs
#write to a temp file, then copy it over the original
    my($oldorg, @newsupported) = @_;
    my($oldfilename);
    my($tempfilename);
    my($line);
    my($linea);
    my($org);
    my($supporting);
    my(@fields);
    $oldfilename = "$templateDirectory/" . "$oldorg" . "-prototype-ini.dat";
    $tempfilename = "$targetDirectory/" . "temp-prototype-ini.dat";
    open(OLDFILE, $oldfilename) ||  die "can't open $oldfilename: $!";
    open(NEWFILE, ">$tempfilename") ||  die "can't open $tempfilename: $!";
    while (defined ($linea = $line = <OLDFILE>)){
	if ($line =~ /^Supporting\b/) {
	    # Use the supporting roles from the first Supporting line for all new orgs
	    while($org = pop(@newsupported)) {
		my($parsed);
		$parsed = 0;
		if (!$parsed) {
		    $parsed = 1;
		    #print $line;
		    @fields = split(/"/, $line); #" this comment is to close the quote in  emacs);
		    #$field[0] == Supporting;
		    #$field[1] == old organization;
		    #$field[2] == spaces
		    #$field[3] == supporting rols
		}
		print NEWFILE "$fields[0]\t \"$org\" \t\t \"$fields[3]\"\n";
	    
	    }
	}
	# copy every line from the old file to the new file.
	print NEWFILE "$linea";
    }
    close(OLDFILE);
    close(NEWFILE);
    rename($tempfilename, $oldfilename);
    
}

# copy file, replace superior, replace supported org
sub newSupport {
    my($oldorg, $neworg, $newsuperior, @newsupported) = @_;
    my($newfilename);
    my($oldfilename);
    my($line);
    my($linea);
    my($org);
    my($supporting);
    my(@fields);
    $oldfilename = "$templateDirectory/" . "$oldorg" . "-prototype-ini.dat";
    $newfilename = "$targetDirectory/" ."$neworg" . "-prototype-ini.dat";
    open(OLDFILE, $oldfilename) ||  die "can't open $oldfilename: $!";
    open(NEWFILE, ">$newfilename") ||  die "can't open $newfilename: $!";
    while (defined ($linea = $line = <OLDFILE>)){
	if ($line =~ /\[UIC\]/) {
	    #print "found UIC line\n";
	    print NEWFILE '[UIC]', "\t\"UIC/$neworg\"\n";
	} elsif ($line =~/^ClusterIdentifier\b/){
	    #print "found ClusterIdentifier line\n";
	    print NEWFILE "ClusterIdentifier\tClusterIdentifier \"$neworg\"";
	} elsif ($line =~/^Superior\b/){
	    #print "found Superior line\n";
	    if ($newsuperior ne '') {
		print NEWFILE "Superior \t \"$newsuperior\"    \"\"\n";
	    }else {
		print NEWFILE $linea;
	    }
	} elsif ($line =~/^Agency\b/) {
	    $line =~ s/CENTCOM/CENTCOMScale/;
	    print NEWFILE $line;
	} elsif ($line =~ /^Supporting\b/) {
	    # This is kind of sloppy because we enter this section many more times
	    # than we actually run it. We only want to run it once.
	    # Use the supporting roles from the first Supporting line for all new orgs
	    # Discard all of the existing supported orgs
	    while($org = pop(@newsupported)) {
		my($parsed);
		$parsed = 0;
		if (!$parsed) {
		    $parsed = 1;
		    #print $line;
		    @fields = split(/"/, $line); #" this comment is to close the quote in  emacs);
		    #$field[0] == Supporting;
		    #$field[1] == old organization;
		    #$field[2] == spaces
		    #$field[3] == supporting rols
		}
		print NEWFILE "$fields[0]\t \"$org\" \t\t \"$fields[3]\"\n";
	    }
	} else { 
	    print NEWFILE $linea; 
	}
    }
    close(NEWFILE);
    close(OLDFILE);
}

# copy the file, but replace the uic 
sub newDotIni {
    my($oldbn, $newbn, $oplanFilename) = @_;
    my($newfilename);
    my($oldfilename);
    my($line);
    $oldfilename = "$templateDirectory/" . "$oldbn" . ".ini";
    $newfilename = "$targetDirectory/" ."$newbn" . ".ini";
    open(OLDFILE, $oldfilename) ||  die "can't open $oldfilename: $!";
    open(NEWFILE, ">$newfilename") ||  die "can't open $newfilename: $!";
    while (defined ($line = <OLDFILE>)){
	if ($line =~ /^uic\b/) {
	    print NEWFILE "uic = UIC/$newbn\n";
	} elsif ($line =~ s/oplan\.xml/$oplanFilename/) {
	    # This is really just for CENTCOM
	    print NEWFILE $line;
	} else {
	    print NEWFILE $line;
	}
    }
    close(NEWFILE);
    close(OLDFILE);
}

# copy the ants inventory files
sub newInv {
    my($oldbn, $newbn) = @_;
    my($newfilename);
    my($oldfilename);
    my($line);
    my(@antstypes);
    my($i);

    @antstypes = ("consumable", "bulkpol", "ammunition");

    for ($i=0; $i<@antstypes; $i++) {
	$oldfilename = "$templateDirectory/" . "$oldbn" . "_" . "$antstypes[$i]" . ".inv";
	if (-e ($oldfilename)) {
	    $newfilename = "$targetDirectory/" ."$newbn" . "_" . "$antstypes[$i]" . ".inv";
	    open(OLDFILE, $oldfilename) || die "can't open $oldfilename: $!\n";
	    open(NEWFILE, ">$newfilename") || die "can't open $newfilename: $!\n";
	    while (defined ($line = <OLDFILE>)){
		print NEWFILE $line;
	    }
	}
    }
    close(NEWFILE);
    close(OLDFILE);
}

#make a copy of a file, replacing the UIC and the Superior
sub newBNProtoIni {
    my($oldprotoini, $newprotoini, $newsuperior) = @_;
    my($newfilename);
    my($oldfilename);
    my($line);
    my($linea);
    $newfilename = "$targetDirectory/" ."$newprotoini" . "-prototype-ini.dat";
    $oldfilename = "$templateDirectory/" . "$oldprotoini" . "-prototype-ini.dat";
    open(OLDFILE, $oldfilename) ||  die "can't open $oldfilename: $!";
    open(NEWFILE, ">$newfilename") ||  die "can't open $newfilename: $!";
    while (defined ($linea = $line = <OLDFILE>)) {
	if ($line =~ /\[UIC\]/) {
	    #print "found UIC line\n";
	    print NEWFILE '[UIC]', "\t\"UIC/$newprotoini\"\n";
	} elsif ($line =~ /^Superior\b/){
	    #print "found Superior line\n";
	    print NEWFILE "Superior\t \"$newsuperior\"   \"\"\n";
	} elsif ($line =~/^ClusterIdentifier\b/){
	    #print "found ClusterIdentifier line\n";
	    print NEWFILE "ClusterIdentifier\tClusterIdentifier \"$newprotoini\"";
	} else { 
	    print NEWFILE $linea; 
	}
    }
    close(NEWFILE);
    close(OLDFILE);
}

sub newDivision {

    my($oldprotoini, $newdivname, @supportedOrgs) = @_;
    my($newfilename);
    my($oldfilename);
    my($line);
    my($linea);
    my($org);
    #print @supportedOrgs;
    $newfilename = "$targetDirectory/" ."$newdivname" . "-prototype-ini.dat";
    $oldfilename = "$templateDirectory/" . "$oldprotoini" . "-prototype-ini.dat";
    open(OLDFILE, $oldfilename) ||  die "can't open $oldfilename: $!";
    open(NEWFILE, ">$newfilename") ||  die "can't open $newfilename: $!";
    while (defined ($linea = $line = <OLDFILE>)) {
	if ($line =~ /\[Relationship\]/) {
	    print NEWFILE "$linea";
	    while ($org = pop(@supportedOrgs)) {
		print NEWFILE "Supporting\t\t \"$org\"\t\t \"StrategicTransportationProvider\"\n";
	    }
	} elsif ($line =~ /^Supporting\b/) {
	} else {
	    print NEWFILE $linea;
	}
    }
    close(OLDFILE);
    close(NEWFILE);
}
sub newNode{
    my($neworgname, @orgs) = @_;
    my($newfilename);
    my($org);

    $newfilename = "$targetDirectory/" ."$neworgname" . "Node.ini";
    open(NEWFILE, ">$newfilename") ||  die "can't open $newfilename: $!";

    print NEWFILE "\[ Clusters \]\n";
    foreach $org (@orgs) {
	print NEWFILE "cluster = $org\n";
    }
    print NEWFILE "\[ AlpProcess \]\n\n";
    print NEWFILE "\[ Policies \]\n\n";
    print NEWFILE "\[ Permission \]\n\n";
    print NEWFILE "\[ AuthorizedOperation \]\n\n";
    close(NEWFILE);
    print "run Node $neworgname" . "Node\n";
}

sub cleanup {
    print `rm -v *99*.ini`;
    print `rm -v *99*-prototype-ini.dat`;
    print `rm -v *99*.inv`;
    print `rm -v *88*.ini`;
    print `rm -v *88*-prototype-ini.dat`;
    print `rm -v *88*.inv`;
    print `rm -v *Scale*-prototype-ini.dat`;
    print `rm -v *Scale*.ini`;
    print `rm -v *Scale*.inv`;
    print `rm -v oplanScale.xml`;
    print "\n";
}

sub startOplan {
    my($oplanFilename) = @_;
    my($oplanPath);
    $oplanPath = "$targetDirectory/" . "$oplanFilename";
    open(OPLANFILE, ">$oplanPath") ||  die "can't open $oplanPath: $!";

    #omit DOCTYPE line

    print OPLANFILE
"<oplan
	oplanID = \"27896\"
	operationName = \"Dessert Run\"
	priority = \"High\"
	cDay = \"07/04/2001\"
	theaterId = \"SWA\"
	terrainType = \"Desert\"
	season = \"Summer\"
	enemyForceType = \"Conventional Forces\"
	hostNationPOLSupport = \"true\"
	hostNationPOLCapability = \"1,000,000 gals/day\"
	hostNationWaterSupport = \"true\"
	hostNationWaterCapability = \"500,000 gals/day\">
	<dfsp>
		<geoloc
			name = \"DFSPScale\"
			code = \"ADKL\"
			lat = \"24.05833\"
			long = \"47.41028\">
		</geoloc>
	</dfsp>
	<apod>
		<geoloc
			name = \"DHAHRAN\"
			code = \"FFTJ\"
			lat = \"26.26389\"
			long = \"50.15833\">
		</geoloc>
	</apod>
	<apod>
		<geoloc
			name = \"KING KHALID MILITARY\"
			code = \"KJAZ\"
			lat = \"27.95778\"
			long = \"45.55306\">
		</geoloc>
	</apod>
	<apod>
		<geoloc
			name = \"KUWAIT INTERNATIONAL AIRPORT\"
			code = \"MMDN\"
			lat = \"29.22611\"
			long = \"47.98056\">
		</geoloc>
	</apod>
	<apod>
		<geoloc
			name = \"FUJAIRAH\"
			code = \"FUAE\"
			lat = \"25.11056\"
			long = \"56.32667\">
		</geoloc>
	</apod>
	<apod>
		<geoloc
			name = \"JUBAIL\"
			code = \"LWEX\"
			lat = \"27.04583\"
			long = \"49.40028\">
		</geoloc>
	</apod>
	<spod>
		<geoloc
			name = \"DAMMAM\"
			code = \"ABFL\"
			lat = \"26.43333\"
			long = \"50.1\">
		</geoloc>
	</spod>
	<spod>
		<geoloc
			name = \"JUBAIL\"
			code = \"LWEV\"
			lat = \"27\"
			long = \"49.6667\">
		</geoloc>
	</spod>
	<organization
		OrgId = \"CENTCOMScale\">
		<orgactivity
			ActivityType = \"Cinc supported\">
		</orgactivity>
	</organization>\n";
}

sub finishOplan {
    print OPLANFILE "</oplan>\n";

    close OPLANFILE;
}

sub addOplanOrg {
    my($neworg) = @_;

    print OPLANFILE "	<organization
		OrgId = \"$neworg\">
		<orgactivity
			ActivityType = \"Deployment\"
			OpTempo = \"Low\">
			<timespan
				startTime = \"C+000\"
				thruTime = \"C+017\">
			</timespan>
			<geoloc
				name = \"TAALoc\"
				code = \"ABAW\"
				lat = \"28.0575\"
				long = \"48.06667\">
			</geoloc>
		</orgactivity>
		<orgactivity
			ActivityType = \"Employment-Defensive\"
			OpTempo = \"Medium\">
			<timespan
				startTime = \"C+018\"
				thruTime = \"C+100\">
			</timespan>
			<geoloc
				name = \"TAALoc\"
				code = \"ABAW\"
				lat = \"28.0575\"
				long = \"48.06667\">
			</geoloc>
		</orgactivity>
		<orgactivity
			ActivityType = \"Employment-Offensive\"
			OpTempo = \"High\">
			<timespan
				startTime = \"C+101\"
				thruTime = \"C+150\">
			</timespan>
			<geoloc
				name = \"TAALoc\"
				code = \"ABAW\"
				lat = \"28.0575\"
				long = \"48.06667\">
			</geoloc>
		</orgactivity>
		<orgactivity
			ActivityType = \"Employment-Standdown\"
			OpTempo = \"Low\">
			<timespan
				startTime = \"C+151\"
				thruTime = \"C+180\">
			</timespan>
			<geoloc
				name = \"TAALoc\"
				code = \"XNLC\"
				lat = \"30.01667\"
				long = \"47.93333\">				
			</geoloc>
		</orgactivity>
	</organization>\n";
}
