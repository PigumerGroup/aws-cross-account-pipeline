import jp.pigumer.sbt.cloud.aws.cloudformation._

val Region         = "ap-northeast-1"
val BucketName     = sys.env.get("BUCKET_NAME")
val KeyId          = "YOUR KEY ID"
val DevelopAccount = "YOUR DEVELOP ACCOUNT ID"

lazy val root = (project in file("."))
  .enablePlugins(CloudformationPlugin)
  .settings(
    organization := "com.pigumer",
    name := "aws-cross-account-pipeline",
    scalaVersion := "2.12.6",
    version := "0.0.1-SNAPSHOT"
  )
  .settings(
    awscfSettings := AwscfSettings(
      region = Region,
      bucketName = BucketName,
      projectName = "crossaccount",
      templates = Some(file("cloudformation"))
    )
  )
  .settings(
    awscfStacks := Stacks(
      Alias("bucket") → CloudformationStack(
        stackName = "crossaccount-bucket",
        template = "bucket.yaml",
        parameters = Map(
          "BucketName" → "jp-pigumer-codepipeline",
          "DevelopAccount" → DevelopAccount,
          "CodeCommitRoleName" → "CrossAccountCodeCommitRole"
        )
      ),
      Alias("codebuild") → CloudformationStack(
        stackName = "crossaccount-codebuild",
        template = "codebuild.yaml",
        parameters = Map(
          "CacheLocation" → BucketName.get,
          "Image" → "pigumergroup/docker-sbt",
          "CodeBuildRoleName" → "CodeBuildRole",
          "Name" → "crossaccount",
          "KeyId" → KeyId
        )
      ),
      Alias("codepipeline") → CloudformationStack(
        stackName = "crossaccount-codepipeline",
        template = "codepipeline.yaml",
        parameters = Map(
          "BucketName" → "jp-pigumer-codepipeline",
          "CodeCommitRoleName" → "CrossAccountCodeCommitRole",
          "CodePipelineRoleName" → "CodePipelineRole",
          "DevelopAccount" → DevelopAccount,
          "Name" → "crossaccount",
          "RepositoryName" → "aws-ecs-fargate",
          "KeyId" → KeyId
        )
      )
    )
  )
