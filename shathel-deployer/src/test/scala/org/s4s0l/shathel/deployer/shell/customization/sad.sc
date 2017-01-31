import org.eclipse.aether.artifact.DefaultArtifact
val reference = new
val m = new DefaultArtifact(s"${reference.getGroup}:${reference.getName}:zip:shathel:${reference.getVersion}")