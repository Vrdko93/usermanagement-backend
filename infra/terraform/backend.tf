terraform {
  backend "s3" {
    bucket       = "um-terraform-state-878113531784"
    key          = "user-management/terraform.tfstate"
    region       = "us-west-1"
    encrypt      = true
    use_lockfile = true
  }
}
